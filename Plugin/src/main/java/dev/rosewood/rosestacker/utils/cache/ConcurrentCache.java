package dev.rosewood.rosestacker.utils.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Manages a concurrent cache which will load data on an as-needed basis.
 * Requires an external timer to call {@link ConcurrentCache#refresh} periodically.
 *
 * @param <K> Key
 * @param <V> Value
 */
public class ConcurrentCache<K, V> {

    private final Map<K, CacheEntry<V>> storage;
    private final Function<K, V> loader;
    private final long cacheDuration;

    public ConcurrentCache(long duration, TimeUnit timeUnit, Function<K, V> loader) {
        this.storage = new ConcurrentHashMap<>();
        this.loader = loader;
        this.cacheDuration = timeUnit.toNanos(duration);
    }

    /**
     * Gets a value from the cache if it exists.
     * Will not cause a load.
     *
     * @param key The key
     * @return the value in cache, or null if none exists for the key
     */
    public V getIfPresent(K key) {
        CacheEntry<V> value = this.storage.get(key);
        return value != null ? value.getEntry() : null;
    }

    /**
     * Gets a value from the cache, causing a load if needed.
     *
     * @param key The key
     * @return the cached value
     */
    public V get(K key) {
        CacheEntry<V> value = this.storage.get(key);

        boolean load = false;
        if (value != null) {
            long expirationTime = value.getInsertionTime() + this.cacheDuration;
            if (System.nanoTime() > expirationTime)
                load = true;
        } else {
            load = true;
        }

        if (load) {
            value = new CacheEntry<>(this.loader.apply(key), System.nanoTime());
            this.storage.put(key, value);
        }

        return value.getEntry();
    }

    /**
     * Causes expired cache entries to unload.
     */
    public void refresh() {
        Iterator<Map.Entry<K, CacheEntry<V>>> entryIterator = this.storage.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<K, CacheEntry<V>> entry = entryIterator.next();
            CacheEntry<V> value = entry.getValue();
            long expirationTime = value.getInsertionTime() + this.cacheDuration;
            if (System.nanoTime() > expirationTime)
                entryIterator.remove();
        }
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        this.storage.clear();
    }

    /**
     * Holds an entry and its insertion time
     * @param <T> The entry type
     */
    private static class CacheEntry<T> {

        private final T entry;
        private final long insertionTime;

        private CacheEntry(T entry, long insertionTime) {
            this.entry = entry;
            this.insertionTime = insertionTime;
        }

        public T getEntry() {
            return this.entry;
        }

        public long getInsertionTime() {
            return this.insertionTime;
        }

    }

}
