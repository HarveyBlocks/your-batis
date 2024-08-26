package org.harvey.batis.scripting.js;

import org.harvey.batis.scripting.xml.DynamicContext;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-25 18:13
 */
public class LanguagePhaser {
    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();
    private static final Map<Class<?>, PropertyAccessor> PROPERTY_ACCESSOR = new HashMap<>();

    public static PropertyAccessor setPropertyAccessor(Class<?> key, PropertyAccessor value) {
        synchronized (PROPERTY_ACCESSOR) {
            return PROPERTY_ACCESSOR.put(key, value);
        }
    }

    public static PropertyAccessor getPropertyAccessor(Object key) {
        return PROPERTY_ACCESSOR.get(getTargetClass(key));
    }

    public static PropertyAccessor getPropertyAccessor(Class<?> key) {
        return PROPERTY_ACCESSOR.get(key);
    }

    public static Object phase(String expression, Map<String, Object> root) {
        ScriptEngine engine = SCRIPT_ENGINE_MANAGER.getEngineByName("js");
        PropertyAccessor accessor = PROPERTY_ACCESSOR.get(getTargetClass(root));
        putParamToEngine(root, accessor, engine);
        Object result;
        try {
            result = engine.eval(expression);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static void putParamToEngine(Map<String, Object> root, PropertyAccessor accessor, ScriptEngine engine) {
        root.keySet().forEach(name -> {
            Object property;
            if (DynamicContext.PARAMETER_OBJECT_KEY.equals(name)) {
                property = root.get(name);
                if (property instanceof Map) {
                    putParamToEngine((Map<String, Object>) property, accessor, engine);
                }
            } else {
                property = accessor.getProperty(null, root, name);
                engine.put(name, property);
            }
        });
    }


    public static Class<?> getTargetClass(Object o) {
        return o == null ? null : (o instanceof Class ? (Class<?>) o : o.getClass());
    }
}
