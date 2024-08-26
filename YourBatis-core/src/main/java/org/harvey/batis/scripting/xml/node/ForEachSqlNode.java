package org.harvey.batis.scripting.xml.node;

import lombok.Getter;
import org.harvey.batis.builder.SqlSourceBuilder;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.builder.BuilderException;
import org.harvey.batis.parsing.GenericTokenParser;
import org.harvey.batis.parsing.TokenHandler;
import org.harvey.batis.scripting.js.LanguagePhaser;
import org.harvey.batis.scripting.js.PropertyAccessor;
import org.harvey.batis.scripting.xml.DynamicContext;
import org.harvey.batis.scripting.xml.SqlNode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ForEachNode
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-12 21:45
 */
public class ForEachSqlNode extends DynamicSqlNode {
    public static final String ITEM_PREFIX = "__foreach_";
    public static final String PARAMETER_OPEN_TOKEN = SqlSourceBuilder.PARAMETER_OPEN_TOKEN;
    public static final String PARAMETER_CLOSE_TOKEN = SqlSourceBuilder.PARAMETER_CLOSE_TOKEN;
    /**
     * 参数名
     */
    private final String collection;
    /**
     * 各个元素名
     */
    private final String item;
    /**
     * 索引名
     */
    private final String index;
    /**
     * openToken
     */
    private final String open;
    /**
     * closeToken
     */
    private final String close;
    /**
     * 分隔符
     */
    private final String separator;

    public ForEachSqlNode(Configuration configuration, SqlNode contents,
                          String collection, String item, String index,
                          String open, String close, String separator) {
        super(configuration, contents);
        this.collection = collection;
        this.item = item;
        this.index = index;
        this.open = open;
        this.close = close;
        this.separator = separator;
    }

    @Override
    public boolean apply(DynamicContext context) {
        Iterable<?> iterable = evaluateIterable(collection, context.getBindings());
        System.out.println(iterable);
        if (!iterable.iterator().hasNext()) {
            return true;
        }
        applyOpen(context);
        boolean first = true;
        int i = 0;
        for (Object o : iterable) {
            DynamicContext oldContext = context;
            // 分隔符加到每一个元素做前缀的方式
            context = new PrefixedContext(context,
                    first || separator == null ? "" : separator);

            int uniqueNumber = context.getUniqueNumber();
            this.applyEach(context, o, i, uniqueNumber);
            contents.apply(new RenamedDynamicContext(
                    configuration, context, index, item, uniqueNumber));
            if (first) {
                // first完成了前缀(分隔符), 就不是first了
                first = !((PrefixedContext) context).isPrefixApplied();
            }

            context = oldContext;
            i++;
        }

        applyClose(context);
        // 去除原生的关系映射
        context.getBindings().remove(item);
        context.getBindings().remove(index);
        // throw new UnfinishedFunctionException(context, configuration, contents);
        return true;
    }



    private static Iterable<?> evaluateIterable(String iterable, Map<String, Object> parameterObject) {
        PropertyAccessor propertyAccessor = LanguagePhaser.getPropertyAccessor(parameterObject);
        Object value = propertyAccessor.getProperty(null, parameterObject, iterable);
        if (value == null) {
            throw new BuilderException("The expression '" + iterable + "' evaluated to a null value.");
        }
        if (value instanceof Iterable) {
            return (Iterable<?>) value;
        }
        if (value.getClass().isArray()) {
            // 数组类型如果是基本数据类型,
            // Arrays.toList()可能抛出ClassCastException
            int size = Array.getLength(value);
            List<Object> answer = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                answer.add(Array.get(value, i));
            }
            return answer;
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).entrySet();
        }
        throw new BuilderException("Error evaluating expression '" + iterable + "'.  Return value (" + value + ") was not iterable.");
    }
    /**
     * @see #applyIndex(DynamicContext, Object, int)
     * @see #applyItem(DynamicContext, Object, int)
     */
    private void applyEach(DynamicContext context,
                           Object itemValue, Object indexValue, int uniqueNumber) {
        // list/set, 索引为index
        if (itemValue instanceof Map.Entry) {
            // map, key为index
            Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) itemValue;
            indexValue = mapEntry.getKey();
            itemValue = mapEntry.getValue();
        }
        // 绑定每一个元素的真实值
        this.applyIndex(context, indexValue, uniqueNumber);
        this.applyItem(context, itemValue, uniqueNumber);
    }
    /**
     * @param context   连接itemName, itemIndex, itemValue
     * @param itemValue 实际值
     * @param itemIndex 实际索引
     */
    private void applyIndex(DynamicContext context, Object itemValue, int itemIndex) {
        if (index != null) {
            context.bind(index, itemValue);
            context.bind(itemizeItem(index, itemIndex), itemValue);
        }
    }

    /**
     * @param context   连接itemName, itemIndex, itemValue
     * @param itemValue 实际值
     * @param itemIndex 实际索引
     */
    private void applyItem(DynamicContext context, Object itemValue, int itemIndex) {
        if (item != null) {
            context.bind(item, itemValue);
            context.bind(itemizeItem(item, itemIndex), itemValue);
        }
    }

    private void applyOpen(DynamicContext context) {
        if (open != null) {
            context.appendSql(open);
        }
    }

    private void applyClose(DynamicContext context) {
        if (close != null) {
            context.appendSql(close);
        }
    }

    /**
     * 将SQL中的原item和index名字转成Bindings里存储的键的名字<br>
     * 通过将{@link DynamicContext}作为{#link delegate}的方式<br>
     * 每次调用{@link SqlNode#apply(DynamicContext)}, <br>
     * 其内部实际上调用{@link DynamicContext#appendSql(String)}来完成SQL组装<br>
     * {@link DynamicContext#appendSql(String)}又是被delegate调用的<br>
     * 最后在本类的{@link #appendSql(String)}中调用delegate的{@link DynamicContext#appendSql(String)}
     * 前后的逻辑, 实际上就是对delegate的{@link DynamicContext#appendSql(String)}的增强<br>
     * 本类包装delegate, 本类的逻辑将作为delegate的增强
     */
    private static class RenamedDynamicContext extends DynamicContext {
        private final DynamicContext delegate;
        private final int index;
        private final String indexName;
        private final String itemName;

        public RenamedDynamicContext(
                Configuration configuration, DynamicContext delegate,
                String indexName, String itemName, int i) {
            super(configuration, null);
            this.delegate = delegate;
            this.index = i;
            this.indexName = indexName;
            this.itemName = itemName;
        }

        @Override
        public Map<String, Object> getBindings() {
            return delegate.getBindings();
        }

        @Override
        public String getSql() {
            return delegate.getSql();
        }

        @Override
        public void appendSql(String sql) {
            // 将SQL中的原item和index名字转成Bindings里存储的键的名字
            TokenHandler renameHandler = content -> {
                String newContent = rename(content, itemName, index);
                if (indexName != null && newContent.equals(content)) {
                    newContent = rename(content, indexName, index);
                }
                return PARAMETER_OPEN_TOKEN + newContent + PARAMETER_CLOSE_TOKEN;
            };
            GenericTokenParser parser = new GenericTokenParser(
                    PARAMETER_OPEN_TOKEN, PARAMETER_CLOSE_TOKEN, renameHandler);

            delegate.appendSql(parser.parse(sql));
        }


        private static String rename(String content, String targetName, int index) {
            return content.replaceFirst(filterRegex(targetName), itemizeItem(targetName, index));
        }

        private static String filterRegex(String name) {
            return "^\\s*" + name + "(?!\\S)";
        }

        @Override
        public void bind(String name, Object value) {
            delegate.bind(name, value);
        }

        @Override
        public int getUniqueNumber() {
            return delegate.getUniqueNumber();
        }
    }

    /**
     * 添加前缀
     */
    private class PrefixedContext extends DynamicContext {
        private final DynamicContext delegate;
        private final String prefix;
        @Getter
        private boolean prefixApplied;

        public PrefixedContext(DynamicContext delegate, String prefix) {
            super(configuration, null);
            this.delegate = delegate;
            this.prefix = prefix;
            this.prefixApplied = false;
        }

        @Override
        public Map<String, Object> getBindings() {
            return delegate.getBindings();
        }


        @Override
        public void appendSql(String sql) {
            if (!prefixApplied && sql != null && !sql.trim().isEmpty()) {
                delegate.appendSql(prefix);
                prefixApplied = true;
            }
            delegate.appendSql(sql);
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
    }


    /**
     * {@link #ITEM_PREFIX}+item_index
     */
    private static String itemizeItem(String item, int index) {
        return ITEM_PREFIX + item + "_" + index;
    }
}
