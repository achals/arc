package com.achals.cache;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by achalshah on 8/1/16.
 */
public class SizeAwareLinkedHashSet<K> extends LinkedHashSet
{
    private int maxSize;

    public SizeAwareLinkedHashSet(final int maxSize)
    {
        super(maxSize);
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(Object o)
    {
        final boolean addStatus = super.add(o);
        while (this.size() > this.maxSize)
        {
            final Iterator<K> iter = this.iterator();
            iter.next();
            iter.remove();
        }
        return addStatus;
    }
}
