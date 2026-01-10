package com.dua3.lumberjack.samples.jcl;

import com.dua3.lumberjack.Lumberjack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {
    static {
        Lumberjack.init();
    }

    public static void main(String[] args) {
        Log log = LogFactory.getLog(Main.class);
        log.info("Hello from JCL!");
    }
}
