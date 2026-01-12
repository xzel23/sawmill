package org.slb4j.samples.jul;

import org.slb4j.SLB4J;

import java.util.logging.Logger;

public class Main {
    static {
        SLB4J.init();
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        logger.info("Hello from JUL!");
    }
}
