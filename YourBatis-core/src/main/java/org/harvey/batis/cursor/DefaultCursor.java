package org.harvey.batis.cursor;

import org.harvey.batis.exception.UnfinishedFunctionException;

import java.io.IOException;
import java.util.Iterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:24
 */
public class DefaultCursor<T> implements Cursor<T>{
    @Override
    public boolean isOpen() {
        throw new UnfinishedFunctionException();
    }

    @Override
    public boolean isConsumed() {
        throw new UnfinishedFunctionException();
    }

    @Override
    public int getCurrentIndex() {
        throw new UnfinishedFunctionException();
    }

    @Override
    public void close() throws IOException {
        throw new UnfinishedFunctionException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnfinishedFunctionException();
    }
}
