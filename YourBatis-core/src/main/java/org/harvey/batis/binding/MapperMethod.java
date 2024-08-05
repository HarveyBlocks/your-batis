package org.harvey.batis.binding;

import lombok.Getter;
import org.harvey.batis.annotation.Flush;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.enums.SqlCommandType;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.binding.BindingException;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.reflection.TypeParameterResolver;
import org.harvey.batis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 14:07
 */
public class MapperMethod {
    private final SqlCommand command;
    private final MethodSignature method;

    /**
     * @param mapperInterface Mapper接口
     * @param method          Mapper接口中的抽象方法
     */
    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(config, mapperInterface, method);
    }

    /**
     * TODO
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        throw new UnfinishedFunctionException(sqlSession, args);
    }

    @Getter
    public static class SqlCommand {
        private final String name;
        private final SqlCommandType type;

        /**
         * TODO
         *
         * @param mapperInterface Mapper接口
         * @param method          Mapper接口中的抽象方法
         */
        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass();
            MappedStatement ms = this.resolveMappedStatement(mapperInterface, methodName, declaringClass, configuration);
            if (ms == null) {
                // 没有完成接口-xml的映射, 是要使用Flush的情况吗?
                if (method.getAnnotation(Flush.class) == null) {
                    // 该方法上没注解了@Flush?
                    throw new BindingException("Invalid bound statement (not found): "
                            + mapperInterface.getName() + "." + methodName);
                }
                name = null;
                type = SqlCommandType.FLUSH;
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (type == SqlCommandType.UNKNOWN) {
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        /**
         * TODO
         * 解析statementId
         */
        private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                       Class<?> declaringClass, Configuration configuration) {
            String statementId = mapperInterface.getName() + "." + methodName;
            if (configuration.hasStatement(statementId)) {
                // 从configuration的mappedStatements中取出该MappedStatement
                // 递归出口
                return configuration.getMappedStatement(statementId);
            }
            if (mapperInterface.equals(declaringClass)) {
                // 方法在在本接口中声明
                // 但是没有存入MappedStatement, 说明没有被完成接口-xml的映射
                // 没有完成映射就没办法实现这个方法
                return null;
            }
            // 方法从父接口中继承
            for (Class<?> superInterface : mapperInterface.getInterfaces()) {
                // 遍历父接口1,父接口2,父接口3...
                if (!declaringClass.isAssignableFrom(superInterface)) {
                    continue;
                }
                // 声明方法的接口是superInterface的父类
                // 递归, 遍历父接口, 祖父接口...
                MappedStatement ms = this.resolveMappedStatement(
                        superInterface, methodName,
                        declaringClass, configuration);
                if (ms != null) {
                    // 递归出口
                    return ms;
                }
            }
            // 方法在所有父接口/祖父接口...中都未声明
            return null;
        }

    }

    /**
     * 方法签名, 存储了方法的信息
     */
    public static class MethodSignature {
        @Getter
        private final boolean returnsMany;
        @Getter
        private final boolean returnsVoid;
        @Getter
        private final boolean returnsCursor;
        @Getter
        private final boolean returnsOptional;
        @Getter
        private final Class<?> returnType;

        /*TODO
        private final boolean returnsMap;
        private final String mapKey;
        private final Integer resultHandlerIndex;
        private final Integer rowBoundsIndex;
        private final ParamNameResolver paramNameResolver;
         */

        /**
         * {@link MethodSignature}
         *
         * @param mapperInterface Mapper接口
         * @param method          Mapper接口中的抽象方法
         */
        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            if (resolvedReturnType instanceof Class<?>) {
                this.returnType = (Class<?>) resolvedReturnType;
            } else if (resolvedReturnType instanceof ParameterizedType) {
                // 取出泛型参数列表, 直接获取类本身
                this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
            } else {
                this.returnType = method.getReturnType();
            }
            this.returnsVoid = void.class.equals(this.returnType);
            this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
            this.returnsCursor = Cursor.class.equals(this.returnType); // 如果它甚至指定了哪种Cursor, 阁下又该如何应对?
            this.returnsOptional = Optional.class.equals(this.returnType);
            /*TODO getMapKey涉及@MapKey注解
            this.mapKey = this.getMapKey(method);
            this.returnsMap = this.mapKey != null;
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            this.paramNameResolver = new ParamNameResolver(configuration, method);*/
        }
    }
}
