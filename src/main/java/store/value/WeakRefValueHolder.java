package store.value;

import java.lang.ref.WeakReference;

public class WeakRefValueHolder<V> implements ValueHolder<V>
{
    private WeakReference<V> weakReference;

    public WeakRefValueHolder(V value)
    {
        if(value == null)
            return;

        weakReference = new WeakReference<>(value);
    }

    @Override
    public V value()
    {
        if(weakReference == null)
            return null;
        return weakReference.get(); // may return null if gc happened.
    }
}
