package org.harvey.batis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 依靠{@link Interceptor}进行一系列的增强
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-19 22:43
 */
public class InterceptorChain {
    /**
     * @see Interceptor
     */
    private final List<Interceptor> interceptors = new ArrayList<>();

    /**
     * 被{@link #interceptors}增强
     *
     * @param target 待增强的类
     * @return 增强之后的类
     */
    public Object pluginAll(Object target) {
        // 依据顺序取出Interceptor
        for (Interceptor interceptor : interceptors) {
            // 每一个Interceptor各自增强
            // 获取增强后的target
            target = interceptor.plugin(target);
        }
        // 返回经过了所有Interceptor增强之后的target
        return target;
    }

    /**
     * 越早添加的{@link Interceptor}, 就越会包裹在执行逻辑的深处<br>
     * 添加顺序 in1, in2, in3
     * 执行顺寻 :<pre>{@code
     * in3_pre->in2_pre->in1_pre->
     * target
     * ->in1_post->in2_post->in3_post
     * }</pre>
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * @return {@link #interceptors}被不可写之后返回
     */
    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
}
