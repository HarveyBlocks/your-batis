package org.harvey.batis.mapping;

import lombok.Getter;
import org.harvey.batis.transaction.TransactionFactory;

import javax.sql.DataSource;

/**
 * 存有当前程序对应的{@link DataSource}和{@link TransactionFactory}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 15:50
 */
@Getter
public class Environment {

    private final DataSource dataSource;

    private final TransactionFactory transactionFactory;

    public Environment(TransactionFactory transactionFactory, DataSource dataSource) {
        if (transactionFactory == null) {
            throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }

    public static class Builder {
        private DataSource dataSource;
        private TransactionFactory transactionFactory;

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        public Environment build() {
            return new Environment(this.transactionFactory, this.dataSource);
        }
    }
}
