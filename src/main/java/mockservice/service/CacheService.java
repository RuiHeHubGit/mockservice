package mockservice.service;

import java.util.Map;

public interface CacheService<T> {
    T get(String key);

    T set(String key, T value);

    CacheService<T> getCacheService(String namespace);

    CacheService<T> getDefaultCacheService();

    Map<String, T> getCache();

}
