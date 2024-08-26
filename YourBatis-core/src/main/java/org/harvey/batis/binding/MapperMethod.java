package org.harvey.batis.binding;

import lombok.Getter;
import org.harvey.batis.annotation.Flush;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.cursor.Cursor;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.binding.BindingException;
import org.harvey.batis.executor.result.ResultHandler;
import org.harvey.batis.mapping.MappedStatement;
import org.harvey.batis.reflection.ParamNameResolver;
import org.harvey.batis.reflection.TypeParameterResolver;
import org.harvey.batis.session.RowBounds;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.util.enums.SqlCommandType;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * TODO
 * 实现Mapper接口的抽象方法
 * 有xml里面定义的sql, 有接口的抽象方法作为字段
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 14:07
 * @see #execute(SqlSession, Object[])
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
     * 实现Mapper接口的抽象方法
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            case SELECT:
                if (method.isReturnsVoid() && method.hasResultHandler()) {
                    throw new UnfinishedFunctionException();
                    // result = executeWithResultHandler(sqlSession, args);
                } else if (method.isReturnsMany()) {
                    result = this.executeForMany(sqlSession, args);
                } else if (method.isReturnsMap()) {
                    throw new UnfinishedFunctionException();
                    // result = executeForMap(sqlSession, args);
                } else if (method.isReturnsCursor()) {
                    throw new UnfinishedFunctionException();
                    // result = executeForCursor(sqlSession, args);
                } else {
                    Object param = method.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(command.getName(), param);
                    if (method.isReturnsOptional()
                            && (result == null || !method.getReturnType().equals(result.getClass()))) {
                        result = Optional.ofNullable(result);
                    }
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType().isPrimitive() && !method.isReturnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    /**
     * 返回值类型处理
     *
     * @param rowCount 返回值
     * @return 将SQL返回值的类型与Mapper接口一致后返回
     */
    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.isReturnsVoid()) {
            return null;
        }
        Class<?> returnType = method.getReturnType();
        if (Integer.class.equals(returnType) || Integer.TYPE.equals(returnType)) {
            result = rowCount;
        } else if (Long.class.equals(returnType) || Long.TYPE.equals(returnType)) {
            result = (long) rowCount;
        } else if (Boolean.class.equals(returnType) || Boolean.TYPE.equals(returnType)) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + returnType);
        }
        return result;
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectList(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectList(command.getName(), param);
        }
        // issue #510 Collections & arrays support
        if (method.getReturnType().isAssignableFrom(result.getClass())) {
            return result;
        }
        if (method.getReturnType().isArray()) {
            // 转变成array
            // return this.convertToArray(result);
            throw new UnfinishedFunctionException();
        }
        // 转换为已声明的集合
        // return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
        throw new UnfinishedFunctionException();
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
                    // 该方法上没注解了@Flush? 那就抛出异常
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
    @Getter
    public static class MethodSignature {
        private final boolean returnsMany;
        private final boolean returnsVoid;
        private final boolean returnsCursor;
        private final boolean returnsOptional;
        private final Class<?> returnType;


        private final boolean returnsMap;
        private final Integer resultHandlerIndex;
        private final Integer rowBoundsIndex;

        private final ParamNameResolver paramNameResolver;
        /*TODO
        private final String mapKey;
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
            this.returnsMap = false;
            /*TODO getMapKey涉及@MapKey注解
            this.mapKey = this.getMapKey(method);
            this.returnsMap = this.mapKey != null;*/
            this.rowBoundsIndex = this.getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = this.getUniqueParamIndex(method, ResultHandler.class);
            this.paramNameResolver = new ParamNameResolver(configuration, method);
        }

        /**
         * @param method    要求method的参数列表里存在最多一个paramType类型的变量
         * @param paramType 目标类型(及其子类)
         * @return paramType类型的变量在method的参数列表中的位置(索引), 没有paramType则返回null
         * @throws BindingException 有多个paramType的参数类型时, 抛出异常
         */
        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            // 遍历method的参数列表
            for (int i = 0; i < argTypes.length; i++) {
                if (!paramType.isAssignableFrom(argTypes[i])) {
                    continue;
                }
                // 是paramType的子类
                if (index != null) {
                    throw new BindingException(method.getName() +
                            " cannot have multiple " +
                            paramType.getSimpleName() +
                            " parameters");
                }
                index = i;
            }
            return index;
        }

        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        /**
         * 将转成函数参数SqlCommand中的参数
         *
         * @see ParamNameResolver#getNamedParams(Object[])
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            return paramNameResolver.getNamedParams(args);
        }


        /**
         * 依据{@link #rowBoundsIndex}从args中获取RowBound<br>
         * 没有就返回null
         */
        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }
    }
}
