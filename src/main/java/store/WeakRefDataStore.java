package store;

import store.value.ValueHolder;
import store.value.WeakRefValueHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeakRefDataStore<K, V> implements DataStore<K, V>
{
    private Map<K, ValueHolder<V>> cacheMap = new ConcurrentHashMap<>();

    @Override
    public ValueHolder<V> get(K key)
    {
        return cacheMap.get(key);
    }

    @Override
    public void put(K key, V value)
    {
        cacheMap.put(key, new WeakRefValueHolder<>(value));
    }

    @Override
    public ValueHolder<V> remove(K key)
    {
        return cacheMap.get(key);
    }

    @Override
    public void clear()
    {
        cacheMap.clear();
    }
}
