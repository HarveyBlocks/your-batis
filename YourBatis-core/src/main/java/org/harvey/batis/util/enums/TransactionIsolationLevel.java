package org.harvey.batis.util.enums;


import java.sql.Connection;

/**
 * <ul>
 *      <li>脏读<br>
 *          对数据库产生更改的事务还<b>未提交</b>, 就被另一个事务录取到了更改后的数据
 *      </li>
 *      <li>不可重复读<br>
 *          在一个事务的两次查询数据库之间, 另一个事务对数据库进行了更改并<b>提交</b>
 *      </li>
 *      <li>幻读<br>
 *          事务没发现这条数据,就插入这条数据; 但是在此查询之后, 插入之前, 另一个事务就已经对数据库插入了这条数据
 *      </li>
 * <ul/>
 * 事务隔离级别
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-31 21:25
 */
public enum TransactionIsolationLevel {
    NONE(Connection.TRANSACTION_NONE),
    /**
     * 已读未提交<br>
     * 不可脏读, 不可重复读, 不可幻读<br>
     * 级别最高
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /**
     * 已读已提交<br>
     * 可脏读, 不可重复读, 不可幻读
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /**
     * 可重复读<br>
     * 可脏读, 可重复读, 不可幻读<br>
     * <b>默认</b>
     */
    REPEATABLE_READ(Connection.TRANSACTION_SERIALIZABLE),
    /**
     * 串行化<br>
     * 可脏读, 可重复读, 可幻读<br>
     * 级别最低
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
    private final int level;

    TransactionIsolationLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public static TransactionIsolationLevel levelOf(int level) {
        switch (level) {
            case Connection.TRANSACTION_NONE:
                return NONE;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return READ_UNCOMMITTED;
            case Connection.TRANSACTION_READ_COMMITTED:
                return READ_COMMITTED;
            case Connection.TRANSACTION_REPEATABLE_READ:
                return REPEATABLE_READ;
            case Connection.TRANSACTION_SERIALIZABLE:
                return SERIALIZABLE;
            default:
                throw new IllegalArgumentException("No level is " + level);
        }
    }
}
