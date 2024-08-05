package org.harvey.batis.cursor;


import java.io.Closeable;

/**
 * TODO
 * <p>
 * 类似于JDBC里的ResultSet类
 * 当查询百万级的数据的时候
 * 使用游标可以节省内存的消耗
 * 不需要一次性取出所有数据
 * 可以进行逐条处理或逐条取出部分批量处理
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-02 13:19
 */
public interface Cursor<T> extends Closeable, Iterable<T> {
    /**
     * @return 当 cursor 开始从数据库中抓取记录时返回true.
     */
    boolean isOpen();

    /**
     * @return 如果 cursor 已使用完毕, 并且已返回与查询匹配的所有记录，则返回 true.
     */
    boolean isConsumed();

    /**
     * @return 返回当前的记录的索引. <b>第一个元素的索引为0</b>, <br>
     * 如果没有检索到第一个 cursor 则返回 -1
     */
    int getCurrentIndex();
}
