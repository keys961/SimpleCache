package cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.DataStore;
import store.value.ValueHolder;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Contain a K-V map for caching
 * @param <K>: Key type
 * @param <V>: Value type
 */
public class SimpleCache<K, V> implements Cache<K, V>
{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCache.class);

    private volatile boolean isClosed;

    private final DataStore<K, V> dataStore;

    private String cacheName;

    private SimpleCacheManager cacheManager;

    private Configuration<K, V> configuration;

    public SimpleCache(final DataStore<K, V> dataStore, String cacheName,
                       SimpleCacheManager cacheManager, Configuration<K, V> configuration)
    {
        this.dataStore = dataStore;
        this.cacheManager = cacheManager;
        this.cacheName = cacheName;
        this.configuration = configuration;
        this.isClosed = false;
    }

    @Override
    public V get(K key)
    {
        ValueHolder<V> valueHolder = dataStore.get(key);
        if(valueHolder == null)
            return null;
        return valueHolder.value();
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys)
    {
        Map<K, V> map = new HashMap<>();
        keys.forEach((key) -> map.put(key, get(key)));
        return map;
    }

    @Override
    public boolean containsKey(K key)
    {
        return get(key) != null;
    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener)
    {
        throw new IllegalArgumentException("loadAll to " + keys + " is not " + "supported by this implementation");
    }

    @Override
    public void put(K key, V value)
    {
        this.dataStore.put(key, value);
    }

    @Override
    public V getAndPut(K key, V value)
    {
        ValueHolder<V> oldVal = dataStore.get(key);
        if(oldVal != null)
            return oldVal.value();

        put(key, value);
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map)
    {
        map.forEach((BiConsumer<K, V>) this::put);
    }

    @Override
    public boolean putIfAbsent(K key, V value)
    {
        return false;
    }

    @Override
    public boolean remove(K key)
    {
        return dataStore.remove(key) != null;
    }

    @Override
    public boolean remove(K key, V oldValue)
    {
        LOG.warn("remove(K, V) is the same as remove(K)!");
        return remove(key);
    }

    @Override
    public V getAndRemove(K key)
    {
        ValueHolder<V> valueHolder = dataStore.remove(key);
        if(valueHolder == null)
            return null;

        return valueHolder.value();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        throw new IllegalArgumentException("replace for " + key
                + " is not " + "supported by this implementation");
    }

    @Override
    public boolean replace(K key, V value)
    {
        throw new IllegalArgumentException("replace for " + key
                + " is not " + "supported by this implementation");
    }

    @Override
    public V getAndReplace(K key, V value)
    {
        throw new IllegalArgumentException("getAndReplace for " + key
                + " is not " + "supported by this implementation");
    }

    @Override
    public void removeAll(Set<? extends K> keys)
    {
        keys.forEach(dataStore::remove);
    }

    @Override
    public void removeAll()
    {
        dataStore.clear();
    }

    @Override
    public void clear()
    {
        dataStore.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz)
    {
        return (C) configuration;
    }

    @Override
    public <T>
        T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException
    {
        throw new IllegalArgumentException("invoke by " + key + " is not " + "supported by this implementation");
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>>
        invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
    {
        throw new IllegalArgumentException("invoke by " + keys + " is not " + "supported by this implementation");
    }

    @Override
    public String getName()
    {
        return cacheName;
    }

    @Override
    public CacheManager getCacheManager()
    {
        return cacheManager;
    }

    @Override
    public void close()
    {
        if (!isClosed) {
            isClosed = true;

            if (cacheManager != null)
                cacheManager.releaseCache(cacheName);
            dataStore.clear();
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

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        throw new IllegalArgumentException("registerCacheEntryListener by " + cacheEntryListenerConfiguration
                + " is not " + "supported by this implementation");
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        throw new IllegalArgumentException("deregisterCacheEntryListener by " + cacheEntryListenerConfiguration
                + " is not " + "supported by this implementation");
    }

    @Override
    public Iterator<Entry<K, V>> iterator()
    {
        throw new IllegalArgumentException("iterator "
                + " is not " + "supported by this implementation");
    }
}
