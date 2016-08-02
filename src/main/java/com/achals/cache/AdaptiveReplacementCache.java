package com.achals.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;

/**
 * Created by achalshah on 7/29/16.
 */
public class AdaptiveReplacementCache<K, V> implements Cache
{
    private final int maxCacheSize;

    private int targetLRUSize;
    private int targetLFUSize;

    private final SizeAwareLinkedHashSet<K> ghostLFU;
    private final SizeAwareLinkedHashMap<K, V> LFU;

    private final SizeAwareLinkedHashSet<K> ghostLRU;
    private final SizeAwareLinkedHashMap<K, V> LRU;

    public AdaptiveReplacementCache(final int cacheSize)
    {
        this.maxCacheSize = cacheSize;
        this.targetLRUSize = cacheSize / 2;
        this.targetLFUSize = cacheSize / 2;

        this.ghostLFU = new SizeAwareLinkedHashSet<>(this.targetLFUSize);
        this.LFU = new SizeAwareLinkedHashMap<>(this.targetLFUSize, this.ghostLFU);

        this.ghostLRU = new SizeAwareLinkedHashSet<>(this.targetLRUSize);
        this.LRU = new SizeAwareLinkedHashMap<>(this.targetLRUSize, this.ghostLRU);
    }

    public Object getIfPresent(Object key)
    {
        return this.getFromMaps(key).get();
    }

    public Object get(Object key, Callable valueLoader) throws ExecutionException
    {
        final Optional<V> optionalCachedValue = this.getFromMaps(key);
        if (optionalCachedValue.isPresent())
        {
            return optionalCachedValue.get();
        }
        final Object value;
        try
        {
            value = valueLoader.call();
        }
        catch (final Exception e)
        {
            throw new ExecutionException(e);
        }
        this.put(key, value);
        return value;
    }

    public void put(Object key, Object value)
    {

    }

    public void putAll(Map m)
    {
        for (final Object object : m.entrySet())
        {
            final Map.Entry<K, V> entry = (Map.Entry<K, V>) object;
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public void invalidate(Object key)
    {

    }

    public void invalidateAll()
    {
        this.ghostLRU.clear();
        this.LRU.clear();
        this.LFU.clear();
        this.ghostLFU.clear();
    }

    public long size()
    {
        return this.LFU.size() + this.LRU.size();
    }

    public CacheStats stats()
    {
        return null;
    }

    public ConcurrentMap asMap()
    {
        return null;
    }

    public void cleanUp()
    {

    }

    public void invalidateAll(Iterable keys)
    {

    }

    public ImmutableMap getAllPresent(Iterable keys)
    {
        return null;
    }

    private Optional<V> getFromMaps(final Object key)
    {
        final V value;
        if (this.LFU.containsKey(key))
        {
            value = (V) this.LRU.get(key);
        }
        else if (this.LRU.containsKey(key))
        {
            value = (V) this.LRU.get(key);
        }
        else
        {
            value = null;
        }
        return Optional.ofNullable(value);
    }
}
