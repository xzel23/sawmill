package org.slb4j;

import org.slb4j.handler.FileHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class LoggingConfigurationTest {

    @Test
    void testFileHandlerConfiguration(@TempDir Path tempDir) {
        Path logFile = tempDir.resolve("test.log");
        Properties props = new Properties();
        props.setProperty("logging.handlers", "file");
        props.setProperty("logging.handler.file.type", "file");
        props.setProperty("logging.handler.file.path", logFile.toString());
        props.setProperty("logging.handler.file.append", "false");
        props.setProperty("logging.handler.file.max-size", "1024");
        props.setProperty("logging.handler.file.max-entries", "100");
        props.setProperty("logging.handler.file.rotation-unit", "DAYS");
        props.setProperty("logging.handler.file.max-backups", "5");
        props.setProperty("logging.handler.file.flush-level", "WARN");
        props.setProperty("logging.handler.file.flush-entries", "10");
        props.setProperty("logging.handler.file.pattern", "%m%n");

        LoggingConfiguration config = LoggingConfiguration.parse(props);

        LogHandler handler = config.getHandlers().stream()
                .filter(h -> "file".equals(h.name()))
                .findFirst()
                .orElseThrow();

        assertInstanceOf(FileHandler.class, handler);
        FileHandler fileHandler = (FileHandler) handler;

        assertEquals(logFile.toAbsolutePath(), fileHandler.getPath().toAbsolutePath());
        assertFalse(fileHandler.isAppend());
        assertEquals(1024L, fileHandler.getMaxFileSize());
        assertEquals(100L, fileHandler.getMaxEntries());
        assertEquals(ChronoUnit.DAYS, fileHandler.getRotationTimeUnit());
        assertEquals(5, fileHandler.getMaxBackupIndex());
        assertEquals(LogLevel.WARN, fileHandler.getFlushLevel());
        assertEquals(10, fileHandler.getFlushEveryNEntries());
        assertEquals("%m%n", fileHandler.getPattern());

        // Test addToProperties
        Properties outProps = new Properties();
        config.addToProperties(outProps);

        assertEquals("file", outProps.getProperty("logging.handlers"));
        assertEquals("file", outProps.getProperty("logging.handler.file.type"));
        assertEquals(logFile.toString(), outProps.getProperty("logging.handler.file.path"));
        assertEquals("false", outProps.getProperty("logging.handler.file.append"));
        assertEquals("1024", outProps.getProperty("logging.handler.file.max-size"));
        assertEquals("100", outProps.getProperty("logging.handler.file.max-entries"));
        assertEquals("DAYS", outProps.getProperty("logging.handler.file.rotation-unit"));
        assertEquals("5", outProps.getProperty("logging.handler.file.max-backups"));
        assertEquals("WARN", outProps.getProperty("logging.handler.file.flush-level"));
        assertEquals("10", outProps.getProperty("logging.handler.file.flush-entries"));
        assertEquals("%m%n", outProps.getProperty("logging.handler.file.pattern"));
    }
}
