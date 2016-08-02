package com.achals.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by achalshah on 8/1/16.
 */
public class SizeAwareLinkedHashMap<K, V> extends LinkedHashMap
{
    private int maxSize;
    private Set<K> ghostSet;

    public SizeAwareLinkedHashMap(final int targetSize,
                                  final Set<K> ghostSet)
    {
        super();
        this.maxSize = targetSize;
        this.ghostSet = ghostSet;
    }

    public void decrementMaxSize()
    {
        this.maxSize--;
    }

    public void incrementMaxSize()
    {
        this.maxSize++;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest)
    {
        if (this.size() > maxSize)
        {
            this.ghostSet.add((K) eldest.getKey());
        }
        return false;
    }
}
