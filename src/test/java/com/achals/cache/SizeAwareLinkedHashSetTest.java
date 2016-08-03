package com.achals.cache;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by achal on 8/2/16.
 */
public class SizeAwareLinkedHashSetTest extends TestCase {

    @Test
    public void test_add_happy_case()
    {
        final int maxSize = 3;
        final SizeAwareLinkedHashSet<Integer> set = new SizeAwareLinkedHashSet<Integer>(maxSize);
        for (int i=0; i<maxSize; i++)
        {
            set.add(i);
        }

        assertEquals(maxSize, set.size());

        System.out.println(set);
        set.add(maxSize);
        assertEquals(maxSize, set.size());
        System.out.println(set);

        for(int i = 1; i <=maxSize; i++)
        {
            assertTrue(String.format("Set should containe %d.", i), set.contains(i));
        }
    }
}
