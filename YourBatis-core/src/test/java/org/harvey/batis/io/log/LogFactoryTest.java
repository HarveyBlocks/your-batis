package org.harvey.batis.io.log;

import junit.framework.TestCase;

public class LogFactoryTest extends TestCase {

    public void testGetLog() {
        Log log = LogFactory.getLog(this.getClass());
        System.out.println(log);
        System.out.println(log.isTraceEnabled());
        System.out.println(log.isDebugEnabled());
        log.trace("hi");
        log.debug("hi");
        log.warn("hi");
        log.error("hi");
    }
}