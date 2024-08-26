package org.harvey.batis.executor.result;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 16:55
 */
public interface ResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);

}