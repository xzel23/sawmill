package com.dua3.sawmill.lumberjack.benchmark;

import com.dua3.sawmill.lumberjack.Lumberjack;
import com.dua3.sawmill.lumberjack.dispatcher.UniversalDispatcher;
import com.dua3.sawmill.lumberjack.handler.ConsoleHandler;
import com.dua3.sawmill.lumberjack.handler.FileHandler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LumberjackBenchmark extends AbstractLoggingBenchmark {

    @Param({"CONSOLE", "FILE"})
    public String category;

    @Param({"SIMPLE", "MDC", "MARKER", "LOCATION", "COLOR"})
    public String format;

    private Logger slf4jLogger;
    private org.apache.logging.log4j.Logger log4jLogger;
    private java.util.logging.Logger julLogger;
    private org.apache.commons.logging.Log jclLogger;
    
    private Marker marker;
    private Path tempFile;
    private FileHandler fileHandler;

    @Override
    protected void setupLogging() throws IOException {
        tempFile = Files.createTempFile("lumberjack-bench", ".log");
        
        String pattern;
        switch (format) {
            case "MDC": pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger [%X{userId}] - %msg%n"; break;
            case "MARKER": pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger (%marker) - %msg%n"; break;
            case "LOCATION": pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger (%file:%line) - %msg%n"; break;
            case "COLOR": pattern = "%Cstart%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger - %msg%Cend%n"; break;
            case "SIMPLE":
            default: pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger - %msg%n"; break;
        }

        UniversalDispatcher dispatcher = UniversalDispatcher.getInstance();
        dispatcher.getLogHandlers().forEach(dispatcher::removeLogHandler);

        if ("CONSOLE".equals(category)) {
            ConsoleHandler consoleHandler = new ConsoleHandler("console", System.out, "COLOR".equals(format));
            consoleHandler.setPattern(pattern);
            dispatcher.addLogHandler(consoleHandler);
        } else {
            fileHandler = new FileHandler("file", tempFile, false);
            fileHandler.setPattern(pattern);
            dispatcher.addLogHandler(fileHandler);
        }

        slf4jLogger = LoggerFactory.getLogger(LumberjackBenchmark.class);
        log4jLogger = org.apache.logging.log4j.LogManager.getLogger(LumberjackBenchmark.class);
        julLogger = java.util.logging.Logger.getLogger(LumberjackBenchmark.class.getName());
        jclLogger = org.apache.commons.logging.LogFactory.getLog(LumberjackBenchmark.class);
        
        marker = MarkerFactory.getMarker("BENCH");
        if ("MDC".equals(format)) {
            org.slf4j.MDC.put("userId", "benchUser");
        }
    }

    @Override
    protected void tearDownLogging() {
        org.slf4j.MDC.clear();
        if (fileHandler != null) {
            fileHandler.close();
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void slf4j() {
        if ("MARKER".equals(format)) {
            slf4jLogger.info(marker, MESSAGE);
        } else {
            slf4jLogger.info(MESSAGE);
        }
    }

    @Benchmark
    public void log4j() {
        log4jLogger.info(MESSAGE);
    }

    @Benchmark
    public void jul() {
        julLogger.info(MESSAGE);
    }

    @Benchmark
    public void jcl() {
        jclLogger.info(MESSAGE);
    }
}
