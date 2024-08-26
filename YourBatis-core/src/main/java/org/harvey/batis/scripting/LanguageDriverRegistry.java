package org.harvey.batis.scripting;

import lombok.Getter;
import org.harvey.batis.exception.scripting.ScriptingException;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:46
 */
public class LanguageDriverRegistry {
    private final Map<Class<? extends LanguageDriver>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    @Getter
    private Class<? extends LanguageDriver> defaultDriverClass;

    public void setDefaultDriverClass(Class<? extends LanguageDriver> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }

    /**
     * @param cls 类对象
     */
    public void register(Class<? extends LanguageDriver> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        // 如果不存在cls为key, 就存入cls实例化后的实体对象
        LANGUAGE_DRIVER_MAP.computeIfAbsent(cls, k -> {
            try {
                return k.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName() +
                        ", need a public NonArgsConstructor", ex);
            }
        });
    }
    public LanguageDriver getDriver(Class<? extends LanguageDriver> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }
}
