package com.dua3.lumberjack.samples.slf4j;

import com.dua3.lumberjack.Lumberjack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    static {
        Lumberjack.init();
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("Hello from SLF4J!");
    }
}
