package store;

import store.value.ValueHolder;

public interface DataStore<K, V>
{
    ValueHolder<V> get(K key);

    void put(K key, V value);

    ValueHolder<V> remove(K key);

    void clear();
}
