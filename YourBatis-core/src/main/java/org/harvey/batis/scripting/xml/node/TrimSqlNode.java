package org.harvey.batis.scripting.xml.node;

import org.harvey.batis.config.Configuration;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

import java.util.*;

/**
 * Trim节点, 增加/去除前后缀
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 17:33
 */
public class TrimSqlNode extends DynamicSqlNode {

    /**
     * 给sql语句拼接的前缀
     */
    private final String prefix;
    /**
     * 给sql语句拼接的后缀
     */
    private final String suffix;
    /**
     * 如果prefixOverrides中的值成为了SQL最前面的语句, 那么会被去掉<br>
     * 有优先级, 索引越小, 越先被检索, 匹配到一个后不再继续删除
     */
    private final List<String> prefixesToOverride;
    /**
     * 如果suffixOverrides中的值成为了SQL最后面的语句, 那么会被去掉<br>
     * 有优先级, 索引越小, 越先被检索, 匹配到一个后不再继续删除
     */
    private final List<String> suffixesToOverride;

    /**
     * @param contents           {@link #contents}
     * @param prefix             {@link #prefix}
     * @param prefixesToOverride {@link #prefixesToOverride}
     * @param suffix             {@link #suffix}
     * @param suffixesToOverride {@link #suffixesToOverride}
     */
    public TrimSqlNode(Configuration configuration, SqlNode contents,
                       String prefix, String prefixesToOverride,
                       String suffix, String suffixesToOverride) {
        this(configuration, contents, prefix, parseOverrides(prefixesToOverride), suffix, parseOverrides(suffixesToOverride));
    }

    /**
     * 解析override字符串, 以"|"分割并全大写
     *
     * @param overrides 形如{@code "a|C|d"}
     * @return {@code ["A","C","D"]}
     */
    private static List<String> parseOverrides(String overrides) {
        if (overrides == null) {
            return Collections.emptyList();
        }
        final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
        final List<String> list = new ArrayList<>(parser.countTokens());
        while (parser.hasMoreTokens()) {
            list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
        }
        return list;
    }

    /**
     * @param contents           {@link #contents}
     * @param prefix             {@link #prefix}
     * @param prefixesToOverride {@link #prefixesToOverride}
     * @param suffix             {@link #suffix}
     * @param suffixesToOverride {@link #suffixesToOverride}
     */
    protected TrimSqlNode(Configuration configuration, SqlNode contents,
                          String prefix, List<String> prefixesToOverride,
                          String suffix, List<String> suffixesToOverride) {
        super(configuration, contents);
        this.prefix = prefix;
        this.prefixesToOverride = prefixesToOverride;
        this.suffix = suffix;
        this.suffixesToOverride = suffixesToOverride;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 包装context, 使其包装后具有检查前后缀的能力
        TrimmedDynamicContext trimmedContext = new TrimmedDynamicContext(context);
        boolean result = contents.apply(trimmedContext);
        trimmedContext.applyTrim();
        return result;
    }

    private class TrimmedDynamicContext extends DynamicContext {
        private final DynamicContext delegate;
        /**
         * prefix只加一遍, 做标记
         */
        private boolean prefixApplied;
        /**
         * suffix只加一遍, 做标记
         */
        private boolean suffixApplied;
        /**
         * trim内部的sql区别父类的{@link DynamicContext#getSql()}保存<br>
         * 完成对trim内部的sql的trim之后, 再一并加入到总sql中去<br>
         */
        private StringBuilder sqlBuffer;

        public TrimmedDynamicContext(DynamicContext delegate) {
            super(configuration, null);
            this.delegate = delegate;
            this.prefixApplied = false;
            this.suffixApplied = false;
            this.sqlBuffer = new StringBuilder();
        }

        /**
         * @param sql 需要被另外保存
         */
        @Override
        public void appendSql(String sql) {
            sqlBuffer.append(sql);
        }

        @Override
        public String getSql() {
            return delegate.getSql();
        }
        @Override
        public void bind(String name, Object value) {
            delegate.bind(name, value);
        }
        @Override
        public int getUniqueNumber() {
            return delegate.getUniqueNumber();
        }

        @Override
        public Map<String, Object> getBindings() {
            return delegate.getBindings();
        }

        public void applyTrim() {
            // 去除头尾空格
            sqlBuffer = new StringBuilder(sqlBuffer.toString().trim());
            // 转大小写之后对sqlBuffer不产生影响
            String upperSql = sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
            if (!upperSql.isEmpty()) {
                this.applyPrefix(sqlBuffer, upperSql);
                this.applySuffix(sqlBuffer, upperSql);
            }
            // 加入原sql
            delegate.appendSql(sqlBuffer.toString());
        }

        private void applyPrefix(StringBuilder sql, String upperSql) {
            if (prefixApplied) {
                return;
            }
            prefixApplied = true;
            if (prefixesToOverride != null) {
                overridePrefixes(sql, upperSql);
            }
            if (prefix != null) {
                addPrefix(sql);
            }
        }

        private void addPrefix(StringBuilder sql) {
            sql.insert(0, " ");
            sql.insert(0, prefix);
        }

        private void overridePrefixes(StringBuilder sql, String upperSql) {
            for (String toRemove : prefixesToOverride) {
                if (upperSql.startsWith(toRemove)) {
                    sql.delete(0, toRemove.trim().length());
                    break;
                }
            }
        }

        private void applySuffix(StringBuilder sql, String upperSql) {
            if (suffixApplied) {
                return;
            }
            suffixApplied = true;
            if (suffixesToOverride != null) {
                overrideSuffixes(sql, upperSql);
            }
            if (suffix != null) {
                addSuffix(sql);
            }
        }

        private void addSuffix(StringBuilder sql) {
            sql.append(" ");
            sql.append(suffix);
        }

        private void overrideSuffixes(StringBuilder sql, String upperSql) {
            for (String toRemove : suffixesToOverride) {
                if (upperSql.endsWith(toRemove) || upperSql.endsWith(toRemove.trim())) {
                    int start = sql.length() - toRemove.trim().length();
                    sql.delete(start, sql.length());
                    break;
                }
            }
        }
    }
}
