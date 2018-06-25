package cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.BasicDataStore;
import store.DataStore;
import store.LRUDataStore;
import store.WeakRefDataStore;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCacheManager implements CacheManager
{
    private final SimpleCacheProvider cacheProvider;

    private final ClassLoader classLoader;

    private final URI uri;

    private final Properties properties;

    private volatile boolean isClosed;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

    private final Map<String, SimpleCache<?, ?>> caches = new ConcurrentHashMap<>();

    public SimpleCacheManager(SimpleCacheProvider cachingProvider, Properties props, ClassLoader classLoader, URI uri)
    {
        this.cacheProvider = cachingProvider;
        this.properties = props;
        this.classLoader = classLoader;
        this.isClosed = false;
        this.uri = uri;
    }

    @Override
    public CachingProvider getCachingProvider()
    {
        return cacheProvider;
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    @Override
    public Properties getProperties()
    {
        return properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V, C extends Configuration<K, V>>
        Cache<K, V> createCache(String cacheName, C configuration) throws IllegalArgumentException
    {
        if (isClosed)
            throw new IllegalStateException();

        checkNotNull(cacheName, "cacheName");
        checkNotNull(configuration, "configuration");

        SimpleCache<?, ?> cache = caches.get(cacheName);

        if (cache == null)
        {
            cache = new SimpleCache<>(getDataStore(properties), cacheName, this, configuration);
            caches.put(cache.getName(), cache);

            return (Cache<K, V>) cache;
        }
        else
            throw new CacheException("A cache named " + cacheName + " already exists.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType)
    {
        if (isClosed)
            throw new IllegalStateException();

        checkNotNull(keyType, "keyType");
        checkNotNull(valueType, "valueType");

        SimpleCache<K, V> cache = (SimpleCache<K, V>) caches.get(cacheName);

        if (cache == null)
            return null;
        else
        {
            Configuration<?, ?> configuration = cache.getConfiguration(Configuration.class);

            if (configuration.getKeyType() != null && configuration.getKeyType().equals(keyType))
            {
                if (configuration.getValueType() != null && configuration.getValueType().equals(valueType))
                    return cache;
                throw new ClassCastException("Incompatible cache value types specified, expected "
                            + configuration.getValueType() + " but " + keyType + " was specified");
            }
            throw new ClassCastException("Incompatible cache key types specified, expected "
                        + configuration.getKeyType() + " but " + valueType + " was specified");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String cacheName)
    {
        return (Cache<K, V>) getCache(cacheName, Object.class, Object.class);
    }

    @Override
    public Iterable<String> getCacheNames()
    {
        if (isClosed)
            throw new IllegalStateException();

        return Collections.unmodifiableList(new ArrayList<>(caches.keySet()));
    }

    @Override
    public synchronized void destroyCache(String cacheName)
    {
        if (isClosed)
            throw new IllegalStateException();

        checkNotNull(cacheName, "cacheName");

        Cache<?, ?> cache = caches.get(cacheName);

        if (cache != null)
            cache.close();
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled)
    {
        throw new IllegalStateException("enableManagement is not supported by this implementation.");
    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled)
    {
        throw new IllegalStateException("enableStatistics is not supported by this implementation.");
    }

    @Override
    public void close()
    {
        if (!isClosed)
        {
            cacheProvider.releaseCacheManager(getURI(), getClassLoader());

            isClosed = true;

            ArrayList<Cache<?, ?>> cacheList = new ArrayList<Cache<?, ?>>(caches.values());
            caches.clear();

            for (Cache<?, ?> cache : cacheList)
            {
                try
                {
                    cache.close();
                }
                catch (Exception e)
                {
                    LOGGER.warn("cannot close cache : " + cache, e);
                }
            }
        }
    }

    @Override
    public boolean isClosed()
    {
        return isClosed;
    }

    @Override
    public <T> T unwrap(Class<T> clazz)
    {
        throw new IllegalArgumentException("Unwrapping to " + clazz + " is not " + "supported by this implementation");
    }

    public synchronized void releaseCache(String cacheName)
    {
        if (cacheName == null)
            throw new NullPointerException();

        caches.remove(cacheName);
    }

    private void checkNotNull(Object object, String name)
    {
        if (object == null)
            throw new NullPointerException(name + " can not be null");
    }

    private DataStore getDataStore(Properties properties)
    {
        if(!properties.containsKey("dataStoreType"))
            return new BasicDataStore();

        String dataStoreType = properties.getProperty("dataStoreType");
        switch (dataStoreType)
        {
            case "basic": return new BasicDataStore();
            case "weakRef": return new WeakRefDataStore();
            case "lru":
            {
                long capacity = Long.parseLong(properties.getProperty("capacity",
                        "32"));

                return new LRUDataStore(capacity);
            }
        }

        return new BasicDataStore();
    }
}
