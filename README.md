# README - A Cache Demo Based on JSR-107
    
#### Framework:

- `CacheProvider`: Manage numbers of `CacheManager`s

- `CacheManager`: Manage numbers of `Cache` entities

- `Cache`: Manage a `DataStore` entity, which contains a K-V map for caching

#### Supported Cache (`DataStore`, stored by reference):

- LRU Cache
    
- Weak Reference Cache
    
- Basic Cache

#### Usage (For LRU usage)

```java
Properties properties = new Properties();
properties.setProperty("dataStoreType", "lru");
properties.setProperty("capacity", "2");
CachingProvider cachingProvider = Caching.getCachingProvider();
//using LRU
CacheManager manager = cachingProvider.getCacheManager(null, null, properties);
Cache<String, Object> cache = manager
                .createCache("Test", new MutableConfiguration<>());
// cache.put()
// cache.get() 
// ...
```