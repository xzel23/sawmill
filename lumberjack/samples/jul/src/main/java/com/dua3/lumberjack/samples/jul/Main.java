package com.dua3.lumberjack.samples.jul;

import com.dua3.lumberjack.Lumberjack;
import java.util.logging.Logger;

public class Main {
    static {
        Lumberjack.init();
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        logger.info("Hello from JUL!");
    }
}
