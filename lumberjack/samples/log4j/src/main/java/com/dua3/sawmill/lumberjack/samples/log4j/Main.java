package com.dua3.sawmill.lumberjack.samples.log4j;

import com.dua3.sawmill.lumberjack.Lumberjack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    static {
        Lumberjack.init();
    }

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger(Main.class);
        logger.info("Hello from Log4j!");
    }
}
