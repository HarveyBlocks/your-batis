package org.harvey.batis.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-18 19:41
 */
public final class ParamNameUtil {
    private ParamNameUtil() {
        super();
    }

    public static List<String> getParamNames(Method method) {
        return ParamNameUtil.getParameterNames(method);
    }

    public static List<String> getParamNames(Constructor<?> constructor) {
        return ParamNameUtil.getParameterNames(constructor);
    }

    private static List<String> getParameterNames(Executable executable) {
        return Arrays.stream(executable.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }


    public static String getParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParameterName(method, paramIndex);
    }

    public static String getParamName(Constructor<?> constructor, int paramIndex) {
        return ParamNameUtil.getParameterName(constructor, paramIndex);
    }

    private static String getParameterName(Executable executable, int paramIndex) {
        return executable.getParameters()[paramIndex].getName();
    }
}