package mockservice.service.impl;

import mockservice.service.CacheService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CacheServiceImpl implements CacheService<Object> {
    private static Map<String, CacheServiceImpl> cacheRepository = new ConcurrentHashMap<>();
    private static String DEFAULT_NAMESPACE = "default";
    private static ReentrantLock namespaceLock = new ReentrantLock();
    private String namespace;
    private volatile Map<String, Object> cache;

    public CacheServiceImpl() {
        this.namespace = DEFAULT_NAMESPACE;
        this.cache = getDefaultCacheService().getCache();
    }

    private CacheServiceImpl(String namespace, Map<String, Object> cache) {
        this.namespace = namespace;
        this.cache = cache;
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public Object set(String key, Object value) {
        return cache.put(key, value);
    }

    @Override
    public CacheService getCacheService(String namespace) {
        if (namespace == null) {
            throw new NullPointerException("namespace required not null");
        }

        try {
            namespaceLock.lock();
            CacheServiceImpl cacheService = cacheRepository.get(namespace);
            if (cacheService == null) {
                if(DEFAULT_NAMESPACE.equals(namespace)) {
                    cacheService = new CacheServiceImpl(DEFAULT_NAMESPACE, new ConcurrentHashMap<>());
                } else {
                    cacheService = new CacheServiceImpl();
                    cacheService.namespace = namespace;
                    cacheService.cache = new ConcurrentHashMap<>();
                }
                cacheRepository.put(namespace, cacheService);
            }
            return cacheService;
        } finally {
            namespaceLock.unlock();
        }
    }

    @Override
    public CacheService getDefaultCacheService() {
        return getCacheService(DEFAULT_NAMESPACE);
    }

    @Override
    public Map<String, Object> getCache() {
        return cache;
    }
}
