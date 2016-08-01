package com.achals.cache;

import java.util.HashMap;
import java.util.HashSet;
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

    private final HashSet<K> ghostLFU;
    private final HashMap<K, V> LFU;
    private final HashMap<K, V> LRU;
    private final HashSet<K> ghostLRU;

    public AdaptiveReplacementCache(final int cacheSize)
    {
        this.maxCacheSize = cacheSize;
        this.targetLRUSize = cacheSize / 2;

        this.ghostLFU = new HashSet<K>();
        this.LFU = new HashMap<K, V>();
        this.LRU = new HashMap<K, V>();
        this.ghostLRU = new HashSet<K>();
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

    public void putAll(Map<K, V> m)
    {
        for (final Map.Entry<K, V> entry: m.entrySet())
        {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public void invalidate(Object key)
    {

    }

    public void invalidateAll()
    {

    }

    public long size()
    {
        return 0;
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
            value = this.LFU.get(key);
        }
        else if (this.LRU.containsKey(key))
        {
            value = this.LRU.get(key);
        }
        else
        {
            value = null;
        }
        return Optional.ofNullable(value);
    }
}
