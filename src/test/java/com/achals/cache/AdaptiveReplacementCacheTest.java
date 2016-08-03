package com.achals.cache;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by achal on 8/3/16.
 */

public class AdaptiveReplacementCacheTest {
    private int maxCacheSize;

    private int targetLRUSize;
    private int targetLFUSize;

    private SizeAwareLinkedHashSet<Integer> ghostLRU;
    private SizeAwareLinkedHashSet<Integer> ghostLFU;
    private SizeAwareLinkedHashMap<Integer, Integer> LRU;
    private SizeAwareLinkedHashMap<Integer, Integer> LFU;

    private AdaptiveReplacementCache<Integer, Integer> cache;

    @Before
    public void setup () {
        this.maxCacheSize = 4;
        this.targetLRUSize = 2;
        this.targetLFUSize = 2;
        this.ghostLFU = new SizeAwareLinkedHashSet<>(this.targetLFUSize);
        this.ghostLRU = new SizeAwareLinkedHashSet<>(this.targetLRUSize);

        this.LRU = new SizeAwareLinkedHashMap<>(this.targetLRUSize, this.ghostLRU);
        this.LFU = new SizeAwareLinkedHashMap<>(this.targetLFUSize, this.ghostLFU);

        this.cache = new AdaptiveReplacementCache<>(this.maxCacheSize, this.ghostLFU, this.LFU, this.ghostLRU, this.LRU);
    }

    @Test
    public void test_put_get_happyCase () {
        this.cache.put(1, 1);
        assertEquals(1, this.cache.getIfPresent(1));
        assertNull(this.cache.getIfPresent(2));
        assertFalse(this.LFU.containsKey(1));
        assertEquals((Integer) 1, this.LRU.get(1));
    }

    @Test
    public void test_put_put_get_happyCase () {
        this.cache.put(1, 1);
        this.cache.put(1, 1);
        assertEquals(1, this.cache.getIfPresent(1));
        assertNull(this.cache.getIfPresent(2));
        assertFalse(this.LRU.containsKey(1));
        assertEquals((Integer) 1, this.LFU.get(1));
    }
}

