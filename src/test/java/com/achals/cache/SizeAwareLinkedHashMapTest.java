package com.achals.cache;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by achalshah on 8/2/16.
 */
public class SizeAwareLinkedHashMapTest
{
    private int maxSize;
    private Set<Integer> ghostSet;
    private SizeAwareLinkedHashMap<Integer, Integer> map;

    @Before
    public void setup()
    {
        this.maxSize = 2;
        this.ghostSet = new HashSet<>();
        this.map = new SizeAwareLinkedHashMap<>(this.maxSize, this.ghostSet);
    }

    @Test
    public void test_add_happyCase()
    {
        this.map.put(1, 1);
        this.map.put(2, 2);

        assertEquals(2, this.map.size());
        assertEquals(0, this.ghostSet.size());

        this.map.put(3, 3);

        assertEquals(2, this.map.size());
        assertEquals(1, this.ghostSet.size());

        assertEquals((Integer) 2, this.map.get(2));
        assertEquals((Integer) 3, this.map.get(3));
        assertTrue(this.ghostSet.contains(1));

        final Iterator<Integer> iter = this.map.keySet().iterator();
        assertTrue(iter.hasNext());
        assertEquals((Integer) 2, iter.next());
    }
}
