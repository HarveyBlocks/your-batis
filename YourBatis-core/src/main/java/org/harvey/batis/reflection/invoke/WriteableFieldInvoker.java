package org.harvey.batis.reflection.invoke;

import java.lang.reflect.Field;

/**
 * 存在Setter的Field
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 15:36
 */
public class WriteableFieldInvoker extends AbstractFieldInvoker {

    public WriteableFieldInvoker(Field field) {
        super(field);
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException {
        return super.fieldInvoke(f -> {
            f.set(target, args[0]);
            return null;
        });
    }

}
