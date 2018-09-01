package com.b2mark.invoice.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogConfig {
    private static Logger logger = LogManager.getLogger();
    public void testOutputLog() {
        logger.info("===========>");
        logger.debug("===========>");
        logger.error("===========>");
        logger.fatal("===========>");
    }
}
