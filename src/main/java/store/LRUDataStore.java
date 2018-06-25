package store;

import store.DataStore;
import store.value.BasicValueHolder;
import store.value.ValueHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LRUDataStore<K, V> implements DataStore<K, V>
{

    private Map<K, LRUEntry<K, ValueHolder<?>>> cacheMap = new ConcurrentHashMap<>();

    /**
     * Least Recently Used is at last
     */
    private LRUEntry<K, ValueHolder<?>> first;

    private LRUEntry<K, ValueHolder<?>> last;

    private Lock lock = new ReentrantLock();

    private final long capacity;

    private long size;

    public LRUDataStore(long capacity)
    {
        this.capacity = capacity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValueHolder<V> get(K key)
    {
        LRUEntry<K, ValueHolder<?>> entry = cacheMap.get(key);
        if(entry != null)
        {
            lock.lock();
            moveToFirst(entry);
            lock.unlock();

            return (ValueHolder<V>) entry.getValue();
        }

        return null;
    }

    @Override
    public void put(K key, V value)
    {
        LRUEntry<K, ValueHolder<?>> entry = cacheMap.get(key);
        if(entry == null)
        {
            // insert
            lock.lock();
            if(size >= capacity)
            {
                // replace
                cacheMap.remove(last.key);
                removeLast();
                size--;
            }
            entry = new LRUEntry<K, ValueHolder<?>>(key, new BasicValueHolder<>(value));
            cacheMap.put(key, entry);
            moveToFirst(entry);
            size++;
            lock.unlock();
        }
        else
        {
            // update
            entry.valueHolder = new BasicValueHolder<>(value);
            lock.lock();
            moveToFirst(entry);
            lock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValueHolder<V> remove(K key)
    {
        ValueHolder<V> valueHolder = null;
        lock.lock();

        LRUEntry<K, ValueHolder<?>> entry = cacheMap.get(key);
        if(entry != null)
        {
            valueHolder = (ValueHolder<V>) entry.getValue();
            if (entry.preEntry != null)
                entry.preEntry.nextEntry = entry.nextEntry;
            if (entry.nextEntry != null)
                entry.nextEntry.preEntry = entry.preEntry;
            if (entry == first)
                first = entry.nextEntry;
            if (entry == last)
                last = entry.preEntry;

            size--;
        }
        lock.unlock();
        cacheMap.remove(key);

        return valueHolder;
    }

    @Override
    public void clear()
    {
        cacheMap.clear();
        this.first = null;
        this.last = null;
    }

    public long getCapacity()
    {
        return capacity;
    }

    public long getSize()
    {
        return size;
    }

    private void moveToFirst(LRUEntry<K, ValueHolder<?>> entry)
    {
        if(entry == first)
            return;

        if(entry.preEntry != null)
            entry.preEntry.nextEntry = entry.nextEntry;
        if(entry.nextEntry != null)
            entry.nextEntry.preEntry = entry.preEntry;
        if(entry == last)
            last = last.preEntry;

        if(first == null || last == null)
        {
            first = entry;
            last = entry;
            return;
        }

        entry.nextEntry = first;
        first.preEntry = entry;
        first = entry;
        entry.preEntry = null;
    }

    private void removeLast()
    {
        if (last != null)
        {
            last = last.preEntry;
            if (last == null)
                first = null;
            else
                last.nextEntry = null;
        }
    }

    class LRUEntry<K, V extends ValueHolder<?>> implements Map.Entry<K, ValueHolder<?>>
    {
        private final K key;

        private ValueHolder<?> valueHolder;

        private LRUEntry<K, ValueHolder<?>> preEntry;

        private LRUEntry<K, ValueHolder<?>> nextEntry;

        public LRUEntry(K key, V value)
        {
            this.key = key;
            this.valueHolder = value;
        }

        public LRUEntry<K, ValueHolder<?>> getPreEntry()
        {
            return preEntry;
        }

        public void setPreEntry(LRUEntry<K, ValueHolder<?>> preEntry)
        {
            this.preEntry = preEntry;
        }

        public LRUEntry<K, ValueHolder<?>> getNextEntry()
        {
            return nextEntry;
        }

        public void setNextEntry(LRUEntry<K, ValueHolder<?>> nextEntry)
        {
            this.nextEntry = nextEntry;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ValueHolder<?> getValue()
        {
            return valueHolder;
        }

        @Override
        public ValueHolder<?> setValue(ValueHolder<?> value)
        {
            valueHolder = null;
            valueHolder = value;
            return value;
        }
    }
}
