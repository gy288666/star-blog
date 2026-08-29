package com.blog.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 轻量内存 TTL 缓存（替代 Redis）：验证码、浏览量去重等。
 * 过期条目在读取时惰性剔除，并有定时清理兜底。
 */
@Component
public class KVCache {

    private record Entry(Object value, long expireAt) {
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void put(String key, Object value, long ttlMillis) {
        store.put(key, new Entry(value, System.currentTimeMillis() + ttlMillis));
    }

    /** 取值，过期或不存在返回 null（惰性删除）。 */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Entry e = store.get(key);
        if (e == null) {
            return null;
        }
        if (e.expireAt() < System.currentTimeMillis()) {
            store.remove(key);
            return null;
        }
        return (T) e.value();
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    /** 取值并删除（一次性，验证码用）。 */
    public <T> T getAndRemove(String key) {
        T v = get(key);
        store.remove(key);
        return v;
    }

    public void remove(String key) {
        store.remove(key);
    }

    /** 每 10 分钟清理过期条目。 */
    @Scheduled(fixedRate = 600_000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expireAt() < now);
    }
}
