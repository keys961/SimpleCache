package cache;

import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manage the CacheManagers
 */
public class SimpleCacheProvider implements CachingProvider
{
    private static final String DEFAULT_URI_STRING = "urn:X-simplecache:jsr107-default-config";

    private static final URI URI_DEFAULT;

    // it can make cache-manager distributable..
    private final Map<ClassLoader, ConcurrentMap<URI, CacheManager>> cacheManagers = new WeakHashMap<>();

    static
    {
        try
        {
            URI_DEFAULT = new URI(DEFAULT_URI_STRING);
        }
        catch (URISyntaxException e)
        {
            throw new javax.cache.CacheException(e);
        }
    }

    @Override
    public void close()
    {
        cacheManagers.values().forEach((map) -> map.values().forEach((CacheManager::close)));
    }

    @Override
    public void close(ClassLoader arg0)
    {
        if(cacheManagers.containsKey(arg0))
            cacheManagers.get(arg0).values().forEach(CacheManager::close);
    }

    @Override
    public void close(URI arg0, ClassLoader arg1)
    {
        if(cacheManagers.containsKey(arg1))
            cacheManagers.get(arg1).forEach (
                    (uri, cacheManager) -> {
                        if(uri.equals(arg0))
                            cacheManager.close();
                    }
            );
    }

    @Override
    public CacheManager getCacheManager()
    {
        return getCacheManager(getDefaultURI(), getDefaultClassLoader(), null);
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader)
    {
        return getCacheManager(uri, classLoader, getDefaultProperties());
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties)
    {
        uri = uri == null ? getDefaultURI() : uri;
        classLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
        properties = properties == null ? new Properties() : cloneProperties(properties);

        ConcurrentMap<URI, CacheManager> cacheManagersByURI = cacheManagers.get(classLoader);

        if (cacheManagersByURI == null) {
            cacheManagersByURI = new ConcurrentHashMap<URI, CacheManager>();
        }

        CacheManager cacheManager = cacheManagersByURI.get(uri);

        if (cacheManager == null)
        {
            cacheManager = new SimpleCacheManager(this, properties, classLoader, uri);
            cacheManagersByURI.put(uri, cacheManager);
        }

        if (!cacheManagers.containsKey(classLoader))
            cacheManagers.put(classLoader, cacheManagersByURI);
        return cacheManager;
    }

    private static Properties cloneProperties(Properties properties)
    {
        Properties clone = new Properties();
        properties.forEach(clone::put);
        return clone;
    }

    @Override
    public ClassLoader getDefaultClassLoader()
    {
        return getClass().getClassLoader();
    }

    @Override
    public Properties getDefaultProperties()
    {
        return new Properties();
    }

    @Override
    public URI getDefaultURI()
    {
        return URI_DEFAULT;
    }

    @Override
    public boolean isSupported(OptionalFeature arg0)
    {
        return false;
    }

    public void releaseCacheManager(URI uri, ClassLoader classLoader)
    {
        if (uri == null || classLoader == null) {
            throw new NullPointerException("uri or classLoader should not be null");
        }

        ConcurrentMap<URI, CacheManager> cacheManagersByURI = cacheManagers.get(classLoader);
        if (cacheManagersByURI != null) {
            cacheManagersByURI.remove(uri);

            if (cacheManagersByURI.size() == 0) {
                cacheManagers.remove(classLoader);
            }
        }
    }
}
