package org.harvey.batis.io.log.impl;

import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 14:28
 */
class Slf4jLocationAwareLoggerImpl implements Log {

    private static final Marker MARKER = MarkerFactory.getMarker(LogFactory.MARKER);

    /**
     * 目标日志的全限定的类名
     */
    private static final String FULL_QUALIFIED_CLASS_NAME = Slf4jImpl.class.getName();

    private final LocationAwareLogger logger;

    Slf4jLocationAwareLoggerImpl(LocationAwareLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        logger.log(MARKER, FULL_QUALIFIED_CLASS_NAME, LocationAwareLogger.ERROR_INT, s, null, e);
    }

    @Override
    public void error(String s) {
        logger.log(MARKER, FULL_QUALIFIED_CLASS_NAME, LocationAwareLogger.ERROR_INT, s, null, null);
    }

    @Override
    public void debug(String s) {
        logger.log(MARKER, FULL_QUALIFIED_CLASS_NAME, LocationAwareLogger.DEBUG_INT, s, null, null);
    }

    @Override
    public void trace(String s) {
        logger.log(MARKER, FULL_QUALIFIED_CLASS_NAME, LocationAwareLogger.TRACE_INT, s, null, null);
    }

    @Override
    public void warn(String s) {
        logger.log(MARKER, FULL_QUALIFIED_CLASS_NAME, LocationAwareLogger.WARN_INT, s, null, null);
    }

}
