package org.harvey.batis.executor.result;

import lombok.Getter;
import org.harvey.batis.reflection.factory.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 将结果存储在{@link #resultList}字段内
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-14 22:02
 */
@Getter
public class DefaultResultHandler implements ResultHandler<Object> {
    private final List<Object> resultList;

    public DefaultResultHandler() {
        resultList = new ArrayList<>();
    }


    /**
     * @see ObjectFactory#create(Class)
     */
    public DefaultResultHandler(ObjectFactory objectFactory) {
        resultList = objectFactory.create(List.class);
    }

    /**
     * 简单地 add -> {@link #resultList}
     *
     * @see ResultContext#getResultObject()
     */
    @Override
    public void handleResult(ResultContext<?> context) {
        resultList.add(context.getResultObject());
    }


}
