package org.harvey.batis.util;

import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:49
 */
@Accessors(chain = true)
public class ErrorContext {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final ThreadLocal<ErrorContext> LOCAL = ThreadLocal.withInitial(ErrorContext::new);

    @Setter
    private String resource;
    private ErrorContext stored;
    @Setter
    private String activity;
    @Setter
    private String object;
    @Setter
    private String message;
    @Setter
    private String sql;
    @Setter
    private Throwable cause;

    private ErrorContext() {
    }

    public static ErrorContext instance() {
        return LOCAL.get();
    }

    /**
     * 创建新实例, 并将当前实例存入新实例的store字段
     * @return 新实例
     */
    public ErrorContext store() {
        // 创建新实例
        ErrorContext newContext = new ErrorContext();
        // 将当前实例作为新实例的字段存储
        newContext.stored = this;
        // 在LOCAL中存入新实例
        LOCAL.set(newContext);
        // 返回新实例
        return LOCAL.get();
    }

    /**
     * 将前一个实例(如果存在)存入ThreadLocal, 并返回; 不存在则返回当前实例
     * @return 如果存在, 前一个实例; 否则返回当前实例
     */
    public ErrorContext recall() {
        if (stored != null) {
            // 如果当前实例存有前一个实例
            // 将前一个实例存入LOCAL
            LOCAL.set(stored);
            stored = null;
        }
        // 返回前一个实例
        return LOCAL.get();
    }

    /**
     * 清空所有字段里的数据(置为null), 调用ThreadLocal.remove()
     * @return 本实例
     */
    public ErrorContext reset() {
        resource = null;
        activity = null;
        object = null;
        message = null;
        sql = null;
        cause = null;
        LOCAL.remove();
        return this;
    }


    /**
     * 本层实例的信息打印
     */
    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        // message
        if (this.message != null) {
            description.append(LINE_SEPARATOR);
            description.append("### ");
            description.append(this.message);
        }

        // resource
        if (resource != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may exist in ");
            description.append(resource);
        }

        // object
        if (object != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may involve ");
            description.append(object);
        }

        // activity
        if (activity != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error occurred while ");
            description.append(activity);
        }

        // sql
        if (sql != null) {
            description.append(LINE_SEPARATOR);
            description.append("### SQL: ");
            description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
        }

        // cause
        if (cause != null) {
            description.append(LINE_SEPARATOR);
            description.append("### Cause: ");
            description.append(cause);
        }

        return description.toString();
    }
}
