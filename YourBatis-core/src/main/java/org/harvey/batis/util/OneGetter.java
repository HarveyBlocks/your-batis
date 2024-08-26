package org.harvey.batis.util;

import org.harvey.batis.exception.TooManyElementsException;

import java.util.Iterator;
import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 23:17
 */
public class OneGetter<E> implements Function<Iterable<E>, E> {
    private final String elementName;
    private final String collectionName;

    public OneGetter() {
        this("elements", "collection");
    }

    public OneGetter(String elementName, String collectionName) {
        this.elementName = elementName;
        this.collectionName = collectionName;
    }

    public E one(Iterable<E> c) {
        if (c == null) {
            return null;
        }
        Iterator<E> it = c.iterator();
        E result = it.hasNext() ? it.next() : null;
        if (it.hasNext()) {
            throw new TooManyElementsException("Too many " + elementName + " in " + collectionName + ".");
        }
        return result;
    }

    @Override
    public E apply(Iterable<E> c) {
        return this.one(c);
    }
}
