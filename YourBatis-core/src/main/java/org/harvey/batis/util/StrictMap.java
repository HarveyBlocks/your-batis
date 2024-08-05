package org.harvey.batis.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * HashMap, 对于同一个Key的写入, 认为是更新; 本类认为同一个Key的写入是异常<br>
 * 同时本类的Key形如{@code school.student.value}, 以.分割<br>
 * 其中{@code school.student.value}是做{@code full name}, {@code value}视作{@code simple name}<br>
 * {@code simple name}也能查到一样的值, 但是在{@code full name}无重复, {@code simple name}有重复的情况下, 认为其具有"多义性",<br>
 * 此时可以存这种多义性的Key, 但不能取, 要取请用{@code full name}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 14:45
 */
public class StrictMap<V> extends HashMap<String, V> {
    private final String name;
    /**
     * 当包含具有相同键的值时，会生成冲突错误消息。<br>
     * 第一个参数是已经存储的Value, 第二个参数是想要存储的参数<br>
     * 返回错误信息
     */
    private BiFunction<V, V, String> conflictMessageProducer;

    public StrictMap(String name, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.name = name;
    }

    public StrictMap(String name, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
    }

    public StrictMap(String name) {
        this.name = name;
    }


    public StrictMap(String name, Map<String, ? extends V> m) {
        super(m);
        this.name = name;
    }

    /**
     * @param conflictMessageProducer {@link #conflictMessageProducer}
     * @return this
     */
    public StrictMap<V> conflictMessageProducer(BiFunction<V, V, String> conflictMessageProducer) {
        this.conflictMessageProducer = conflictMessageProducer;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws IllegalArgumentException 当key有重复时
     */
    @Override
    @SuppressWarnings("unchecked") // 将(V) new Ambiguity(shortKey)的类型转换警告给消除
    public V put(String key, V value) {
        if (containsKey(key)) {
            throw new IllegalArgumentException(name + " already contains value for " + key
                    + runConflictMessageProducer(key, value));
        }
        if (key.contains(".")) {
            final String simpleKey = StrictMap.getSimpleName(key);
            // 🤔 : 为什么这个cast是可以的? 不怕ClassCastException?
            // 答 : 嘿, 还真不会有ClassCastException, 我现在才知道😂

            // 在key的情况下是唯一的, 在key转变为simpleKey的时候, 就不唯一了
            // 这种情况, 称这个simpleKey具有多义性(Ambiguity), 即模棱两可的
            // 需要对这个simpleKey的键值对做特殊的存储(标记为Ambiguity)
            V v = (V) new Ambiguity(simpleKey);
            super.put(simpleKey, super.get(simpleKey) == null ? value : v);
        }
        return super.put(key, value);
    }

    /**
     * {@link #conflictMessageProducer}为null则返回空字符串, 否则执行{@link #conflictMessageProducer}
     */
    private String runConflictMessageProducer(String key, V value) {
        return conflictMessageProducer == null ? "" : conflictMessageProducer.apply(super.get(key), value);
    }

    /**
     * @param key school.student.value
     * @return value
     */
    private static String getSimpleName(String key) {
        final String[] keyParts = key.split("\\.");
        return keyParts[keyParts.length - 1];
    }

    /**
     * 多义性, 模棱两可的情况, 特别记
     */
    @Getter
    @AllArgsConstructor
    private static class Ambiguity {
        private final String subject;
    }

    /**
     * {@inheritDoc}
     * 在{@code full name}无重复, {@code simple name}有重复的情况下, 认为其具有"多义性",<br>
     * 此时可以存这种多义性的Key, 但不能取, 要取请用{@code full name}
     *
     * @param key the key whose associated value is to be returned
     * @throws IllegalArgumentException key重复或多义性时
     */
    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            // 值不存在
            throw new IllegalArgumentException(name + " does not contain value for " + key);
        }
        if (value instanceof Ambiguity) {
            // 多义性的值可以存, 但不能取
            throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                    + " (try using the full name including the namespace, or rename one of the entries)");
        }
        return value;
    }


}
