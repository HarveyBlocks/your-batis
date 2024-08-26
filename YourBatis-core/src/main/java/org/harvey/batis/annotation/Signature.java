package org.harvey.batis.annotation;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:50
 */

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指示方法的签名
 *
 * @author Clinton Begin
 * @see Intercepts
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {
    /**
     * @return java type
     */
    Class<?> type();

    /**
     * {@link #type()}中的 method name.
     */
    String method();

    /**
     * {@link #method()} ()}中的method的args的类型.
     */
    Class<?>[] args();
}