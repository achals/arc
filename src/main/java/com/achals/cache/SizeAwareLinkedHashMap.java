package com.achals.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by achalshah on 8/1/16.
 */
public class SizeAwareLinkedHashMap<K, V> extends LinkedHashMap<K, V>
{
    private int maxSize;
    private Set<K> ghostSet;

    public SizeAwareLinkedHashMap(final int targetSize,
                                  final Set<K> ghostSet)
    {
        super(targetSize);
        this.maxSize = targetSize;
        this.ghostSet = ghostSet;
    }

    public void decrementMaxSize()
    {
        if (this.maxSize > 1) {
            this.maxSize--;
        }
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
            return true;
        }
        return false;
    }
}
