package org.harvey.batis.annotation;

import java.lang.annotation.*;

/**
 * 指定要拦截的目标方法, 被截获的方法将被执行增强
 * <pre>
 * &#064;Intercepts({&#064;Signature(
 *   type= Executor.class,
 *   method = "update",
 *   args = {MappedStatement.class ,Object.class})})
 * public class ExamplePlugin implements Interceptor {
 *   &#064;Override
 *   public Object intercept(Invocation invocation) throws Throwable {
 *     // implement pre-processing if needed
 *     Object returnObject = invocation.proceed();
 *     // implement post-processing if needed
 *     return returnObject;
 *   }
 * }
 * </pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:49
 * @see Signature
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {
    /**
     * @return 要截获的方法签名, method signatures
     * @see Signature
     */
    Signature[] value();
}

