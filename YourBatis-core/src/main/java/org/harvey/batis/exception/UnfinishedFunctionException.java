package org.harvey.batis.exception;

import org.harvey.batis.util.ConsoleColorfulString;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 未完成的操作的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-01 14:08
 */
public class UnfinishedFunctionException extends YourbatisException {
    public UnfinishedFunctionException(Object... ignored) {
        super();
    }

    public UnfinishedFunctionException() {
        super();
    }

    public UnfinishedFunctionException(String message) {
        super(message);
    }

    public UnfinishedFunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnfinishedFunctionException(Throwable cause) {
        super(cause);
    }

    public UnfinishedFunctionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    private static final PrintStream TRACE = new PrintStream(System.out) {
        @Override
        public void print(String s) {
            String painting = ConsoleColorfulString.painting(s, ConsoleColorfulString.Color.GRAY);
            super.print(painting);
        }
    };

    public static void trace(String... msg) {
        new UnfinishedFunctionException("暂无" + Arrays.toString(msg) + "需求").printStackTrace(TRACE);
    }

    public static final Set<String> TRACE_CACHE = new HashSet<>();

    @Override
    public void printStackTrace(PrintStream s) {
        StackTraceElement[] stackTrace = this.getStackTrace();
        String trace = this.getClass() + ": " + this.getMessage() +
                "\n\tat" + stackTrace[1];
        if (TRACE_CACHE.contains(trace)) {
            return;
        }
        TRACE_CACHE.add(trace);
        s.println(trace + (stackTrace.length > 2 ? "\n\tat" + stackTrace[2] : ""));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UnfinishedFunctionException)) {
            return false;
        }
        UnfinishedFunctionException that = (UnfinishedFunctionException) obj;
        String name = this.getClass().getName();
        StackTraceElement[] thisStackTrace = this.getStackTrace();
        StackTraceElement[] thatStackTrace = that.getStackTrace();
        int length = thisStackTrace.length;
        if (length != thatStackTrace.length) {
            return false;
        }
        for (int traceIndex = 1; traceIndex < length; traceIndex++) {
            StackTraceElement thisTrace = thisStackTrace[traceIndex];
            String thisClass = thisTrace.getClassName();
            StackTraceElement thatTrace = thatStackTrace[traceIndex];
            String thatClass = thatTrace.getClassName();
            boolean equalClassName = Objects.equals(thatClass, thisClass);
            if (!equalClassName) {
                return false;
            }
            if (thatClass.equals(name)) {
                continue;
            }
            int thisLine = thisTrace.getLineNumber();
            int thatLine = thatTrace.getLineNumber();
            return Objects.equals(thisLine, thatLine);
        }
        return true;
    }


}
