package com.achals.cache;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void test_constructor()
    {
        assertNotNull(new AdaptiveReplacementCache<Integer, Integer>(6));
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

    @Test
    public void test_put_new_to_ghost_LRU () {
        for (int i = 0; i < 4; i++) {
            this.cache.put(i, i);
        }

        assertTrue(this.LFU.isEmpty());
        assertEquals(2, this.LRU.size());
        assertEquals(2, this.ghostLRU.size());

        assertTrue(this.ghostLRU.contains(0));
        assertTrue(this.ghostLRU.contains(1));

        assertEquals(2, this.cache.getIfPresent(2));
        assertEquals(3, this.cache.getIfPresent(3));
    }

    @Test
    public void test_put_same_to_ghost_LRU () {
        for (int i = 0; i < 4; i++) {
            this.cache.put(i, i);
            this.cache.put(i, i);
        }

        assertTrue(this.LRU.isEmpty());
        assertEquals(2, this.LFU.size());
        assertEquals(2, this.ghostLFU.size());

        assertTrue(this.ghostLFU.contains(0));
        assertTrue(this.ghostLFU.contains(1));

        assertEquals(2, this.cache.getIfPresent(2));
        assertEquals(3, this.cache.getIfPresent(3));
    }

    @Test
    public void test_put_add_to_LFU_readd () {
        this.cache.put(1, 1);
        this.cache.put(1, 1);
        this.cache.put(2, 2);
        this.cache.put(2, 2);

        assertEquals(2, this.LFU.size());
        assertEquals(1, this.cache.getIfPresent(1));
        assertEquals(2, this.cache.getIfPresent(2));

        this.cache.put(1, 1);
        this.cache.put(3, 3);
        this.cache.put(3, 3);

        assertEquals(2, this.LFU.size());
        assertEquals(1, this.cache.getIfPresent(1));
        assertEquals(3, this.cache.getIfPresent(3));

        assertTrue(this.ghostLFU.contains(2));
    }

    @Test
    public void test_put_add_to_ghostLRU_readd () {
        this.cache.put(1, 1);
        this.cache.put(2, 2);
        this.cache.put(3, 3);

        assertEquals(2, this.LRU.size());
        assertEquals(3, this.cache.getIfPresent(3));
        assertEquals(2, this.cache.getIfPresent(2));

        this.cache.put(1, 1);

        assertEquals(3, this.LRU.size());
        assertEquals(1, this.cache.getIfPresent(1));
        assertEquals(2, this.cache.getIfPresent(2));
        assertEquals(3, this.cache.getIfPresent(3));

        assertTrue(this.ghostLRU.isEmpty());
    }

    @Test
    public void test_put_add_to_ghostLFU_readd () {
        for (int i = 1; i <= 3; i++) {
            this.cache.put(i, i);
            this.cache.put(i, i);
        }

        assertTrue(this.LRU.isEmpty());
        assertEquals(2, this.LFU.size());
        assertEquals(3, this.cache.getIfPresent(3));
        assertEquals(2, this.cache.getIfPresent(2));

        this.cache.put(1, 1);

        assertEquals(3, this.LFU.size());
        assertEquals(1, this.cache.getIfPresent(1));
        assertEquals(2, this.cache.getIfPresent(2));
        assertEquals(3, this.cache.getIfPresent(3));

        assertTrue(this.ghostLFU.isEmpty());
    }


    @Test
    public void test_get_entry_absent_loaded() throws Exception
    {
        final Callable<Integer> callable = () -> 1;

        this.cache.get(1, callable);

        assertEquals(1, this.cache.getIfPresent(1));
    }

    @Test
    public void test_get_entry_present_not_loaded() throws Exception
    {
        final Callable<Integer> callable = mock(Callable.class);

        this.cache.put(1, 1);
        this.cache.get(1, callable);

        assertEquals(1, this.cache.getIfPresent(1));
        verifyZeroInteractions(callable);
    }

    @Test(expected = ExecutionException.class)
    public void test_get_entry_absent_exception() throws Exception
    {
        final Callable<Integer> callable = () -> {throw new Exception();};

        this.cache.get(1, callable);
    }

}

