package org.harvey.batis.annotation;


import java.lang.annotation.*;

/**
 * 通过 Mapper 接口调用 flush 语句的 maker 注解。
 * 🤔 : 有了本注解的方法就不用在XML里映射?
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 17:07
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Flush {
}
