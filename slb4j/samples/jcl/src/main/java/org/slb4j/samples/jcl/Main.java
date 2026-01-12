package org.slb4j.samples.jcl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slb4j.SLB4J;

public class Main {
    static {
        SLB4J.init();
    }

    public static void main(String[] args) {
        Log log = LogFactory.getLog(Main.class);
        log.info("Hello from JCL!");
    }
}
