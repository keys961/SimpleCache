package store.value;

public class BasicValueHolder<V> implements ValueHolder<V>
{
    private final V value;

    public BasicValueHolder(V value)
    {
        this.value = value;
    }

    @Override
    public V value()
    {
        return this.value;
    }
}
