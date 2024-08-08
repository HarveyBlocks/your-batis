package org.harvey.batis.parsing;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 默认的错误处理器: 忽略警告, 抛出异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 21:54
 */
public class DefaultErrorHandler implements ErrorHandler {

    @Override
    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void warning(SAXParseException exception) {
        // 啥也不做
        exception.printStackTrace(System.err);
    }
}

