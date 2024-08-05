package org.harvey.batis.reflection.invoke;

import java.lang.reflect.Field;

/**
 * 存在Getter的Field
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 15:36
 */
public class ReadableFieldInvoker extends AbstractFieldInvoker {
    public ReadableFieldInvoker(Field field) {
        super(field);
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException {
        return super.fieldInvoke(f -> f.get(target));
    }
}
