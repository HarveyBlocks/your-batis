package org.harvey.batis.cache;

import org.harvey.batis.exception.cache.CacheException;
import org.harvey.batis.util.ArrayUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 实例化{@link #CacheKey(Object...)}
 * 实现hashcode,equals, 是应对更全面, 更强大的key
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 21:12
 */
public class CacheKey implements Cloneable, Serializable {
    public static final CacheKey NULL_CACHE_KEY = new CacheKey() {
        @Override
        public void update(Object object) {
            throw new CacheException("Not allowed to update a null cache key instance.");
        }

        @Override
        public void updateAll(Object... objects) {
            throw new CacheException("Not allowed to update a null cache key instance.");
        }
    };
    /**
     * 默认基数, 这里的基数是进制中的概念, 类似与37进制
     */
    private static final int DEFAULT_MULTIPLIER = 37;
    /**
     * 默认Hash码(的初始值)
     */
    private static final int DEFAULT_HASHCODE = 17;

    /**
     * @see #update(Object)
     */
    private int hashcode;
    /**
     * {@link #CacheKey(Object...)}<br>
     * 参数的所有实体的hash值都会累加在这上面<br>
     * 如果有元素Object是null, hash值会是1
     */
    private long checksum;
    /**
     * {@link #CacheKey(Object...)}<br>
     * 参数的所有实体个数统计
     */
    private int count;
    /**
     * {@link #CacheKey(Object...)}<br>
     * 参数的所有实体的存储
     */
    private List<Object> updateList;


    public CacheKey(Object... objects) {
        this.hashcode = DEFAULT_HASHCODE;
        this.count = 0;
        this.updateList = new ArrayList<>();
        if (objects == null || objects.length == 0) {
            return;
        }
        this.updateAll(objects);
    }


    public void updateAll(Object... objects) {
        for (Object o : objects) {
            this.update(o);
        }
    }

    public void update(Object object) {
        int baseHashCode = object == null ? 1 : ArrayUtil.hashCode(object);
        count++;
        checksum += baseHashCode;
        hashcode = DEFAULT_MULTIPLIER * hashcode + baseHashCode * count;
        updateList.add(object);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        // 比较类型
        if (!(object instanceof CacheKey)) {
            return false;
        }

        final CacheKey cacheKey = (CacheKey) object;

        // 比较hashcode
        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        // 比较checksum
        if (checksum != cacheKey.checksum) {
            return false;
        }
        // 比较count
        if (count != cacheKey.count) {
            return false;
        }
        // 比较updateList中每个元素按顺序各自比较
        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (!ArrayUtil.equals(thisObject, thatObject)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see #update(Object)
     */
    @Override
    public int hashCode() {
        return hashcode;
    }

    /**
     * @return <pre>{@code
     *  "hashcode:checksum:updateList[0]:...."
     * }</pre>
     * @see #hashcode
     * @see #checksum
     * @see #updateList
     */
    @Override
    public String toString() {
        StringJoiner returnValue = new StringJoiner(":");
        returnValue.add(String.valueOf(hashcode));
        returnValue.add(String.valueOf(checksum));
        updateList.stream().map(ArrayUtil::toString).forEach(returnValue::add);
        return returnValue.toString();
    }

    @Override
    public CacheKey clone() throws CloneNotSupportedException {
        // 将hashcode和checksum直接拷贝
        CacheKey clonedCacheKey = (CacheKey) super.clone();
        // 将ArrayList深拷贝
        clonedCacheKey.updateList = new ArrayList<>(this.updateList);
        return clonedCacheKey;
    }

}