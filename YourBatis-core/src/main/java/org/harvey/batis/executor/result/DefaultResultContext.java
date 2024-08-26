package org.harvey.batis.executor.result;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 22:03
 */
public class DefaultResultContext<T> implements ResultContext<T> {

    private T resultObject;
    private int resultCount;
    private boolean stopped;

    public DefaultResultContext() {
        resultObject = null;
        resultCount = 0;
        stopped = false;
    }

    @Override
    public T getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    /**
     * @param resultObject 保存到字段{@link #resultObject}
     */
    public void nextResultObject(T resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

}
