package org.harvey.batis.io.log;

import org.harvey.batis.exception.io.LogException;
import org.harvey.batis.io.log.impl.Slf4jImpl;

import java.lang.reflect.Constructor;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 13:16
 */
public class LogFactory {
    /**
     * 实现要使用的标记
     */
    public static final String MARKER = "YOURBATIS";


    private static Constructor<? extends Log> logConstructor;
    private LogFactory() {
        // private 静态工具类
    }

    public static Log getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }


    static {
        tryImplementation(LogFactory::useSlf4jLogging);
        /*暂不实现
        tryImplementation(LogFactory::useCommonsLogging);
        tryImplementation(LogFactory::useLog4J2Logging);
        tryImplementation(LogFactory::useLog4JLogging);
        tryImplementation(LogFactory::useJdkLogging);
        tryImplementation(LogFactory::useNoLogging);*/
    }

    private static void tryImplementation(Runnable runnable) {
        if (logConstructor == null) {
            // 使创建的时候有一个优先级, 如果创建失败就使用后面的日志继续创建
            try {
                runnable.run();
            } catch (Throwable ignore) {
            }
        }
    }

    public static synchronized void useSlf4jLogging() {
        LogFactory.setImplementation(Slf4jImpl.class);
    }

    /**
     * 一个百分百错误的日志
     * @deprecated
     */
    public static synchronized void useIllegalLogging() {
        LogFactory.setImplementation(Log.class);
    }

    private static void setImplementation(Class<? extends Log> implClass) {
        try {
            Constructor<? extends Log> candidate = implClass.getConstructor(String.class);// 候选人
            Log log = candidate.newInstance(LogFactory.class.getName()); // 实例化
            if (log.isDebugEnabled()) {
                log.debug("Logging initialized using '" + implClass + "' adapter.");
            }
            logConstructor = candidate; // 实例化成功者, 存入logConstructor字段
        } catch (Throwable t) {
            throw new LogException("Error setting Log implementation.  Cause: " + t, t);
        }
    }

    public static Log getLog(String logName) {
        try {
            return logConstructor.newInstance(logName);
        } catch (Throwable t) {
            throw new LogException("Error creating logger for logger " + logName + ".  Cause: " + t, t);
        }
    }

}
