package org.harvey.batis.reflection.invoke;

import lombok.Getter;
import org.harvey.batis.reflection.Reflector;
import org.harvey.batis.util.function.ThrowableFunction;

import java.lang.reflect.Field;

/**
 * ReadableFieldInvoker和WriteableFieldInvoker的共同特征提取
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 15:40
 */
@Getter
public abstract class AbstractFieldInvoker implements Invoker {
    private final Field field;

    public AbstractFieldInvoker(Field field) {
        this.field = field;
    }

    protected Object fieldInvoke(ThrowableFunction<Field, Object, IllegalAccessException> function)
            throws IllegalAccessException {
        try {
            return function.apply(field);
        } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
                field.setAccessible(true);
                return function.apply(field);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
