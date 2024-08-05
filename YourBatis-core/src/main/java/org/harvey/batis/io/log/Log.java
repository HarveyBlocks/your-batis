package org.harvey.batis.io.log;


/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 13:15
 */
public interface Log {
    boolean isDebugEnabled();

    boolean isTraceEnabled();

    void error(String s, Throwable e);

    void error(String s);

    void debug(String s);

    void trace(String s);

    void warn(String s);

    default void debugIfEnable(String s) {
        if (this.isDebugEnabled()) {
            this.debug(s);
        }
    }
    default void traceIfEnable(String s) {
        if (this.isTraceEnabled()) {
            this.trace(s);
        }
    }
}
