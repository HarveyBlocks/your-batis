package org.harvey.batis.io;

import org.harvey.batis.io.log.Log;
import org.harvey.batis.io.log.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 访问应用程序资源的类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-07 17:31
 */
public class ResourceAccessorFactory{

    private static final Log LOG = LogFactory.getLog(ResourceAccessor.class);

    /**
     * 内置的实现
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends ResourceAccessor>[] IMPLEMENTATIONS = new Class[]{DefaultResourceAccessor.class};

    /**
     * 调用{@link #addImplementation(Class)}将加入本List, 由用户添加一些实现类
     */
    public static final List<Class<? extends ResourceAccessor>> USER_IMPLEMENTATIONS = new ArrayList<>();





    /**
     * 单例持有者, 决定单例对象是谁<br>
     * 保证只有在{@link #getInstance()}时调用此类, <br>
     * 才能让{@link #INSTANCE}能选择是哪个{@link ResourceAccessor}的子类被实例化
     */
    private static class AccessorHolder {
        public static final ResourceAccessor INSTANCE = createAccessor();


        private static ResourceAccessor createAccessor() {
            // 将要被实例化的Accessor
            List<Class<? extends ResourceAccessor>> implementations = new ArrayList<>();
            // 先用后面加上来的
            implementations.addAll(USER_IMPLEMENTATIONS);
            // 再用内置的
            implementations.addAll(Arrays.asList(IMPLEMENTATIONS));
            ResourceAccessor accessor = null;
            for (int i = 0; accessor == null || !accessor.isValid(); i++) {
                // 不断遍历直到accessor.isValid()==true, 有效的时候
                Class<? extends ResourceAccessor> implementation = implementations.get(i);
                try {
                    // 实例化这个实现类
                    accessor = implementation.getDeclaredConstructor().newInstance();
                    if (!accessor.isValid()) {
                        // 无效, 记录日志
                        LOG.debugIfEnable("Application Server Resource Accessor implementation " + implementation.getName() + " is not valid in this environment.");
                    }
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                         InvocationTargetException e) {
                    LOG.error("Failed to instantiate " + implementation, e);
                    return null;
                    // Why return null? 不应该往继续尝试下一个实现类吗?
                    // 因为实现类没有给出无参构造, 才会被catch
                    // 大抵是为了防止成功运行而让用户没有发觉自己的实现有问题,
                    // 从而造成潜在的Bug
                    // 才这样设计的吧?
                }
                // 继续尝试下一个
            }
            // 找到了一个实现类
            LOG.debugIfEnable("Using Application Server Resource Accessor adapter " + accessor.getClass().getName());

            return accessor;
        }
    }

    /**
     * 如果没有当前环境依然有效的 {@link ResourceAccessor} 存在<br>
     * 则返回null<br>
     *
     * @return 单例
     */
    public static ResourceAccessor getInstance() {
        return AccessorHolder.INSTANCE;
    }

    /**
     * 将实现类添加到 {@link ResourceAccessorFactory#USER_IMPLEMENTATIONS} 的列表中。<br>
     * 以这种方式添加的类, 将按照添加的顺序进行尝试实例化，并且在任何内置实现类之前进行尝试。<br>
     * (在调用{@link ResourceAccessorFactory#getInstance()}之前添加)
     *
     * @param accessorClass {@link ResourceAccessor} 的实现类, <b>要求必须有一个无参构造</b>
     */
    public static void addImplementation(Class<? extends ResourceAccessor> accessorClass) {
        if (accessorClass != null) {
            USER_IMPLEMENTATIONS.add(accessorClass);
        }
    }

}
