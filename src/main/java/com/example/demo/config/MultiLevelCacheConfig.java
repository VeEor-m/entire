package com.example.demo.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存管理器
 * 一级: Caffeine (本地缓存)
 * 二级: Redis (分布式缓存)
 */
public class MultiLevelCacheManager implements CacheManager {

    private final CacheManager caffeineCacheManager;
    private final CacheManager redisCacheManager;
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    public MultiLevelCacheManager(CacheManager caffeineCacheManager, CacheManager redisCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, n -> new MultiLevelCache(
                caffeineCacheManager.getCache(n),
                redisCacheManager.getCache(n)
        ));
    }

    @Override
    public Collection<String> getCacheNames() {
        return caffeineCacheManager.getCacheNames();
    }

    /**
     * 多级缓存实现
     * 读取时: 先从Caffeine读，没有则从Redis读并写入Caffeine
     * 写入时: 同时写入Caffeine和Redis
     */
    public static class MultiLevelCache implements Cache {

        private final Cache localCache;
        private final Cache remoteCache;

        public MultiLevelCache(Cache localCache, Cache remoteCache) {
            this.localCache = localCache;
            this.remoteCache = remoteCache;
        }

        @Override
        public String getName() {
            return localCache.getName();
        }

        @Override
        public Object getNativeCache() {
            return remoteCache.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper wrapper = localCache.get(key);
            if (wrapper == null) {
                // 一级缓存没有，尝试二级缓存
                wrapper = remoteCache.get(key);
                if (wrapper != null) {
                    // 写入一级缓存
                    localCache.put(key, wrapper.get());
                }
            }
            return wrapper;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = localCache.get(key, type);
            if (value == null) {
                value = remoteCache.get(key, type);
                if (value != null) {
                    localCache.put(key, value);
                }
            }
            return value;
        }

        @Override
        public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
            T value = localCache.get(key, valueLoader);
            if (value == null) {
                value = remoteCache.get(key, valueLoader);
                if (value != null) {
                    localCache.put(key, value);
                }
            }
            return value;
        }

        @Override
        public void put(Object key, Object value) {
            localCache.put(key, value);
            remoteCache.put(key, value);
        }

        @Override
        public void evict(Object key) {
            localCache.evict(key);
            remoteCache.evict(key);
        }

        @Override
        public void clear() {
            localCache.clear();
            remoteCache.clear();
        }
    }
}
