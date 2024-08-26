package org.harvey.batis.builder.xml;

import org.harvey.batis.builder.MapperBuilderAssistant;
import org.harvey.batis.builder.MethodResolver;
import org.harvey.batis.builder.ResultMapResolver;
import org.harvey.batis.config.Configuration;
import org.harvey.batis.exception.UnfinishedFunctionException;
import org.harvey.batis.exception.builder.IncompleteElementException;
import org.harvey.batis.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-09 15:05
 */
public class MapperBuilder {
    private final Configuration configuration;
    /**
     * 有Mapper文件的路径
     */
    private final MapperBuilderAssistant assistant;
    private final Class<?> type;


    public <T> MapperBuilder(Configuration configuration, Class<T> type) {
        // 需要从类的包名获取路径, 然后再获取同路径下的Mapper.xml文件
        String resource = type.getName().replace('.', '/') + ".?????????????????????";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
        UnfinishedFunctionException.trace("检查注释", "解析注解");
    }

    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            this.loadXmlResource();
            configuration.addLoadedResource(resource);
            // 再次检验
            assistant.setCurrentTargetMapper(type.getName());
            /* 无需求
            parseCache();
            parseCacheRef();*/
            UnfinishedFunctionException.trace("Cache");
            UnfinishedFunctionException.trace("解析注解");
            /* 解析注解 ... ?*/
        }
        configuration.parsePendingRemoveFinished(configuration.getIncompleteMethods(), MethodResolver::resolve,false);
    }

    private void loadXmlResource() {
        // 在XMLMapperBuilder#bindMapperForNamespace中添加标识
        if (configuration.isResourceLoaded("namespace:" + type.getName())) {
            // 防止重复加载
            return;
        }
        String xmlResource = type.getName().replace('.', '/') + ".xml";
        // 在本module中查询xml文件
        InputStream inputStream = type.getResourceAsStream("/" + xmlResource);
        if (inputStream == null) {
            // 搜索不在本module中，而是在类路径中的 XML 映射器。
            try {
                inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
            } catch (IOException e2) {
                // ignore, Mapper.xml不是必须的
            }
        }
        if (inputStream == null) {
            return;
        }
        XMLMapperBuilder xmlParser = new XMLMapperBuilder(
                inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
        xmlParser.parse();
    }
}
