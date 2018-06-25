import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import bean.User;
import org.junit.Test;

import java.util.Properties;

public class SimpleCacheTest
{
    @Test
    public void testSimpleCache()
    {
        Properties properties = new Properties();
        properties.setProperty("dataStoreType", "lru");
        properties.setProperty("capacity", "2");
        CachingProvider cachingProvider = Caching.getCachingProvider();
        //using LRU
        CacheManager manager = cachingProvider.getCacheManager(null, null, properties);
        Cache<String, User> cache = manager
                .createCache("Test", new MutableConfiguration<>());


        String key = "leo";
        User user = new User();
        user.setName("leo");

        String key1 = "liu";
        User user1 = new User();
        user1.setName("liu");

        String key2 = "robin";
        User user2 = new User();
        user2.setName("robin");

        cache.put(key, user);
        cache.put(key1, user1);
        cache.get(key);
        cache.put(key2, user2);

        // hit
        if (cache.get(key) != null) {
            System.out.println("Hello " + cache.get(key).getName());
        }
        // miss
        if (cache.get(key1) != null) {
            System.out.println("Hello " + cache.get(key1).getName());
        }
        // hit
        if (cache.get(key2) != null) {
            System.out.println("Hello " + cache.get(key2).getName());
        }
    }
}
