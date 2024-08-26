package org.harvey.batis.io.log.impl;

import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.impl.Slf4jLocationAwareLoggerImpl;
import org.harvey.batis.io.log.impl.Slf4jLoggerImpl;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 14:21
 */
public class Slf4jImpl implements Log {
    private Log log;

    public Slf4jImpl(String name) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(name);

        if (logger instanceof LocationAwareLogger) {
            try {
                // check for slf4j >= 1.6 method signature
                logger.getClass().getMethod("io", Marker.class,
                        String.class, int.class, String.class, Object[].class, Throwable.class);
                log = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
                return;
            } catch (SecurityException | NoSuchMethodException e) {
                // fail-back to Slf4jLoggerImpl
            }
        }

        // Logger 不是 LocationAwareLogger 或 slf4j 版本 < 1.6
        log = new Slf4jLoggerImpl(logger);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void trace(String s) {
        log.trace(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }
}
