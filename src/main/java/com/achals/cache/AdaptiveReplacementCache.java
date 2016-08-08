package com.achals.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by achalshah on 7/29/16.
 */
public class AdaptiveReplacementCache<K, V> implements Cache {
    private final int maxCacheSize;
    private final SizeAwareLinkedHashSet<K> ghostLFU;
    private final SizeAwareLinkedHashMap<K, V> LFU;
    private final SizeAwareLinkedHashSet<K> ghostLRU;
    private final SizeAwareLinkedHashMap<K, V> LRU;
    private int targetLRUSize;
    private int targetLFUSize;

    public AdaptiveReplacementCache (final int maxCacheSize,
                                     final SizeAwareLinkedHashSet<K> ghostLFU,
                                     final SizeAwareLinkedHashMap<K, V> LFU,
                                     final SizeAwareLinkedHashSet<K> ghostLRU,
                                     final SizeAwareLinkedHashMap<K, V> LRU) {
        this.maxCacheSize = maxCacheSize;

        this.targetLFUSize = this.maxCacheSize / 2;
        this.targetLRUSize = this.maxCacheSize / 2;

        this.ghostLFU = ghostLFU;
        this.LFU = LFU;
        this.ghostLRU = ghostLRU;
        this.LRU = LRU;
    }

    public AdaptiveReplacementCache (final int cacheSize) {
        this.maxCacheSize = cacheSize;
        this.targetLRUSize = cacheSize / 2;
        this.targetLFUSize = cacheSize / 2;

        this.ghostLFU = new SizeAwareLinkedHashSet<>(this.targetLFUSize);
        this.LFU = new SizeAwareLinkedHashMap<>(this.targetLFUSize, this.ghostLFU);

        this.ghostLRU = new SizeAwareLinkedHashSet<>(this.targetLRUSize);
        this.LRU = new SizeAwareLinkedHashMap<>(this.targetLRUSize, this.ghostLRU);
    }

    public Object getIfPresent (Object key) {
        final Optional<V> optionalVal = this.getFromMaps(key);
        if (optionalVal.isPresent()) {
            return optionalVal.get();
        }
        return null;
    }

    public Object get (final Object key, final Callable valueLoader) throws ExecutionException {
        final Optional<V> optionalCachedValue = this.getFromMaps(key);
        if (optionalCachedValue.isPresent()) {
            return optionalCachedValue.get();
        }
        final Object value;
        try {
            value = valueLoader.call();
        } catch (final Exception e) {
            throw new ExecutionException(e);
        }
        this.put(key, value);
        return value;
    }

    public void put (Object key, Object value) {
        if (this.LFU.containsKey(key)) {
            this.LFU.remove(key);
            this.LFU.put((K) key, (V) value);
            return;
        }
        if (this.LRU.containsKey(key)) {
            this.LRU.remove(key);
            this.LFU.put((K) key, (V) value);
            return;
        }
        if (this.ghostLRU.contains(key)) {
            this.LRU.incrementMaxSize();
            this.LFU.decrementMaxSize();
            this.ghostLRU.remove(key);
            this.LRU.put((K) key, (V) value);
            return;
        }
        if (this.ghostLFU.contains(key)) {
            this.LRU.decrementMaxSize();
            this.LFU.incrementMaxSize();
            this.ghostLFU.remove(key);
            this.LFU.put((K) key, (V) value);
            return;
        }
        this.LRU.put((K) key, (V) value);
    }

    public void putAll (Map m) {
        for (final Object object : m.entrySet()) {
            final Map.Entry<K, V> entry = (Map.Entry<K, V>) object;
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public void invalidate (Object key) {
        if (this.LRU.containsKey(key)) {
            this.LRU.remove(key);
        } else if (this.LFU.containsKey(key)) {
            this.LFU.remove(key);
        } else if (this.ghostLRU.contains(key)) {
            this.ghostLRU.remove(key);
        } else if (this.ghostLFU.contains(key)) {
            this.ghostLFU.remove(key);
        }
    }

    public void invalidateAll () {
        this.ghostLFU.clear();
        this.LFU.clear();
        this.LRU.clear();
        this.ghostLRU.clear();
    }

    public long size () {
        return this.LFU.size() + this.LRU.size();
    }

    public CacheStats stats () {
        return null;
    }

    public ConcurrentMap asMap () {
        return null;
    }

    public void cleanUp () {

    }

    public void invalidateAll (Iterable keys) {
        for (final Object key: keys) {
            this.invalidate(key);
        }
    }

    public ImmutableMap getAllPresent (Iterable keys) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (final Object key : keys) {
            if (this.LRU.containsKey(key)) {
                builder.put((K) key, (V) this.LRU.get(key));
            }
            if (this.LFU.containsKey(key)) {
                builder.put((K) key, (V) this.LFU.get(key));
            }
        }
        return builder.build();
    }

    private Optional<V> getFromMaps (final Object key) {
        final V value;
        if (this.LFU.containsKey(key)) {
            value = (V) this.LFU.get(key);
        } else if (this.LRU.containsKey(key)) {
            value = (V) this.LRU.get(key);
        } else {
            value = null;
        }
        return Optional.ofNullable(value);
    }
}
