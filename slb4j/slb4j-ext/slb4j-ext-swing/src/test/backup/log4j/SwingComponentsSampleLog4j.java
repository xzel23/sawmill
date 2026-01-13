package com.dua3.utility.samples.log4j;

import org.slb4j.LogLevel;

/**
 * SwingComponentsSampleLog4j class extends the SwingComponentsSampleLogBase.
 *
 * <p>This implementation uses the Log4J logging framework.
 */
public final class SwingComponentsSampleLog4j extends SwingComponentsSampleLogBase {
    static {
        LogUtilLog4J.init(LogLevel.TRACE);
    }

    private SwingComponentsSampleLog4j() { /* nothing to do */ }

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        start(SwingComponentsSampleLog4j::new);
    }
}
