package org.harvey.batis.builder;

import lombok.Getter;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.io.Resources;
import org.harvey.batis.util.enums.ParameterMode;
import org.harvey.batis.util.type.TypeHandler;
import org.harvey.batis.util.type.TypeHandlerRegistry;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 16:10
 */
public abstract class BaseBuilder {
    @Getter
    protected final Configuration configuration;
    /*TODO
    protected final TypeAliasRegistry typeAliasRegistry;*/
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        UnfinishedFunctionException.trace("注册别名");
        /*TODO
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();*/
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    /**
     * 将类名实例成字节码类
     *
     * @return null if javaType is null
     * @see Resources#classForName(String)
     */
    protected static <T> Class<? extends T> resolveClass(String javaType) {
        if (javaType == null) {
            return null;
        }
        try {
            return (Class<T>) Resources.classForName(javaType);
        } catch (ClassNotFoundException e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    protected ParameterMode resolveParameterMode(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return ParameterMode.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
        }
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
        if (typeHandlerAlias == null) {
            return null;
        }
        Class<?> type = BaseBuilder.resolveClass(typeHandlerAlias);
        if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
            // 不存在类型获取类型不是TypeHandler的子类
            throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface");
        }
        Class<? extends TypeHandler<?>> typeHandlerType = (Class<? extends TypeHandler<?>>) type;
        return resolveTypeHandler(javaType, typeHandlerType);
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null) {
            return null;
        }
        // 从已注册的TypeHandler中获取
        TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
        if (handler != null) {
            // 没有被注册, 那就实例化
            return handler;
        }
        // 实例化typeHandlerType
        return typeHandlerRegistry.getInstance(javaType, typeHandlerType);
    }
}
