package com.dua3.lumberjack.handler;

import com.dua3.lumberjack.LogLevel;
import com.dua3.lumberjack.MDC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testBasicLogging() throws IOException {
        Path logFile = tempDir.resolve("test.log");
        try (FileHandler handler = new FileHandler("test", logFile, false)) {
            handler.setPattern("%msg%n");
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Hello, World!", "location", null);
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Second line", "location", null);
        }

        assertTrue(Files.exists(logFile));
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertEquals("Hello, World!", lines.get(0));
        assertEquals("Second line", lines.get(1));
    }

    @Test
    void testAppend() throws IOException {
        Path logFile = tempDir.resolve("test-append.log");
        Files.writeString(logFile, "Initial content\n");

        try (FileHandler handler = new FileHandler("test", logFile, true)) {
            handler.setPattern("%msg%n");
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Second line", "location", null);
        }

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertEquals("Initial content", lines.get(0));
        assertEquals("Second line", lines.get(1));
    }

    @Test
    void testReplace() throws IOException {
        Path logFile = tempDir.resolve("test-replace.log");
        Files.writeString(logFile, "Initial content\n");

        try (FileHandler handler = new FileHandler("test", logFile, false)) {
            handler.setPattern("%msg%n");
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "New content", "location", null);
        }

        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());
        assertEquals("New content", lines.get(0));
    }

    @Test
    void testSizeRotation() throws IOException {
        Path logFile = tempDir.resolve("test-size.log");
        try (FileHandler handler = new FileHandler("test", logFile, false)) {
            handler.setPattern("%msg%n");
            handler.setMaxFileSize(10); // Very small size to trigger rotation quickly
            handler.setMaxBackupIndex(2);

            // Each log entry is "Line X\n" which is about 7 bytes.
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 1", "location", null); // ~7 bytes
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 2", "location", null); // ~14 bytes -> rotation should occur BEFORE or AFTER?
            // In my implementation, checkRotation is called BEFORE writing.
            // 1st entry: size 0, max 10 -> no rotate. Write "Line 1\n". currentSize = 7.
            // 2nd entry: size 7, max 10 -> no rotate. Write "Line 2\n". currentSize = 14.
            // 3rd entry: size 14, max 10 -> ROTATE. Write "Line 3\n" to NEW file.
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 3", "location", null);
        }

        assertTrue(Files.exists(logFile));
        assertTrue(Files.exists(tempDir.resolve("test-size.log.1")));

        List<String> currentLines = Files.readAllLines(logFile);
        assertEquals(1, currentLines.size());
        assertEquals("Line 3", currentLines.get(0));

        List<String> backupLines = Files.readAllLines(tempDir.resolve("test-size.log.1"));
        assertEquals(2, backupLines.size());
        assertEquals("Line 1", backupLines.get(0));
        assertEquals("Line 2", backupLines.get(1));
    }

    @Test
    void testEntryRotation() throws IOException {
        Path logFile = tempDir.resolve("test-entries.log");
        try (FileHandler handler = new FileHandler("test", logFile, false)) {
            handler.setPattern("%msg%n");
            handler.setMaxEntries(2);
            handler.setMaxBackupIndex(2);

            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 1", "location", null);
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 2", "location", null);
            // 3rd entry triggers rotation because currentEntries (2) >= maxEntries (2)
            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 3", "location", null);
        }

        assertTrue(Files.exists(logFile));
        assertTrue(Files.exists(tempDir.resolve("test-entries.log.1")));

        assertEquals("Line 3", Files.readAllLines(logFile).get(0));
        assertEquals("Line 1", Files.readAllLines(tempDir.resolve("test-entries.log.1")).get(0));
        assertEquals("Line 2", Files.readAllLines(tempDir.resolve("test-entries.log.1")).get(1));
    }

    @Test
    void testTimeRotation() throws IOException, InterruptedException {
        Path logFile = tempDir.resolve("test-time.log");
        try (FileHandler handler = new FileHandler("test", logFile, false)) {
            handler.setPattern("%msg%n");
            // Use a very small time unit if possible, but ChronoUnit.SECONDS is the smallest truncatedTo supports usually
            handler.setRotationTimeUnit(ChronoUnit.SECONDS);

            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 1", "location", null);

            // Wait for next second
            Thread.sleep(1100);

            handler.handle(Instant.now(), "test", LogLevel.INFO, null, null, () -> "Line 2", "location", null);
        }

        assertTrue(Files.exists(logFile));
        assertTrue(Files.exists(tempDir.resolve("test-time.log.1")));
    }
}
