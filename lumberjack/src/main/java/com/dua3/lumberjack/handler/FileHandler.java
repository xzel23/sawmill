/*
 * Copyright 2026 Axel Howind - axh@dua3.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.lumberjack.handler;

import com.dua3.lumberjack.Location;
import com.dua3.lumberjack.LogFilter;
import com.dua3.lumberjack.LogHandler;
import com.dua3.lumberjack.LogLevel;
import com.dua3.lumberjack.LogPattern;
import com.dua3.lumberjack.MDC;
import org.jspecify.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A log handler that writes log entries to a file.
 * It supports log rotation triggered by file size, number of entries, or time.
 */
public class FileHandler implements LogHandler, AutoCloseable {

    private final String name;
    private final Path path;
    private final boolean append;
    private final LogPattern logPattern = new LogPattern();
    private volatile LogFilter filter = LogFilter.allPass();

    private @Nullable PrintStream out;
    private long currentSize;
    private long currentEntries;
    private @Nullable Instant nextRotationTime;

    private long maxFileSize = -1;
    private long maxEntries = -1;
    private @Nullable ChronoUnit rotationTimeUnit;
    private int maxBackupIndex = 1;

    /**
     * Constructs a new FileHandler.
     *
     * @param name   the name of the handler
     * @param path   the path to the log file
     * @param append if true, then bytes will be written to the end of the file rather than the beginning
     * @throws IOException if the file cannot be opened
     */
    public FileHandler(String name, Path path, boolean append) throws IOException {
        this.name = name;
        this.path = path;
        this.append = append;
        openFile();
    }

    private synchronized void openFile() throws IOException {
        if (out != null) {
            out.close();
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StandardOpenOption[] options;
        if (append) {
            options = new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
        } else {
            options = new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
        }

        this.out = new PrintStream(new BufferedOutputStream(Files.newOutputStream(path, options)), true, StandardCharsets.UTF_8);
        this.currentSize = Files.size(path);
        this.currentEntries = 0;
        updateNextRotationTime();
    }

    private void updateNextRotationTime() {
        if (rotationTimeUnit != null) {
            nextRotationTime = Instant.now().truncatedTo(rotationTimeUnit).plus(1, rotationTimeUnit);
        } else {
            nextRotationTime = null;
        }
    }

    /**
     * Sets the maximum file size before rotation.
     *
     * @param maxFileSize the maximum file size in bytes, or -1 for no limit
     */
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Sets the maximum number of entries before rotation.
     *
     * @param maxEntries the maximum number of entries, or -1 for no limit
     */
    public void setMaxEntries(long maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Sets the rotation time unit.
     *
     * @param rotationTimeUnit the time unit for rotation, or null for no time-based rotation
     */
    public void setRotationTimeUnit(@Nullable ChronoUnit rotationTimeUnit) {
        this.rotationTimeUnit = rotationTimeUnit;
        updateNextRotationTime();
    }

    /**
     * Sets the maximum number of backup files to keep.
     *
     * @param maxBackupIndex the maximum number of backup files
     */
    public void setMaxBackupIndex(int maxBackupIndex) {
        this.maxBackupIndex = maxBackupIndex;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public synchronized void handle(Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, @Nullable Location location, Supplier<String> msg, @Nullable Throwable t) {
        if (filter.test(instant, loggerName, lvl, mrk, mdc, location, msg, t)) {
            checkRotation(instant);
            if (out != null) {
                logPattern.formatLogEntry(out, instant, loggerName, lvl, mrk, mdc, location, msg, t, null);
                out.flush();
                try {
                    currentSize = Files.size(path);
                } catch (IOException e) {
                    // Fallback if we cannot get size
                    currentSize += 100; // estimated
                }
                currentEntries++;
            }
        }
    }

    private void checkRotation(Instant now) {
        boolean rotate = false;
        if (maxFileSize > 0 && currentSize >= maxFileSize) {
            rotate = true;
        } else if (maxEntries > 0 && currentEntries >= maxEntries) {
            rotate = true;
        } else if (nextRotationTime != null && !now.isBefore(nextRotationTime)) {
            rotate = true;
        }

        if (rotate) {
            try {
                rotate();
            } catch (IOException e) {
                System.err.println("Error during log rotation: " + e.getMessage());
            }
        }
    }

    private synchronized void rotate() throws IOException {
        if (out != null) {
            out.close();
            out = null;
        }

        // Rename existing backup files
        for (int i = maxBackupIndex - 1; i >= 1; i--) {
            Path src = getBackupPath(i);
            Path dest = getBackupPath(i + 1);
            if (Files.exists(src)) {
                Files.move(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // Rename current file to .1
        if (Files.exists(path)) {
            Files.move(path, getBackupPath(1), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        openFile();
    }

    private Path getBackupPath(int index) {
        String fileName = path.getFileName().toString();
        String newFileName = fileName + "." + index;
        return path.resolveSibling(newFileName);
    }

    @Override
    public void setFilter(LogFilter filter) {
        this.filter = Objects.requireNonNull(filter);
    }

    @Override
    public LogFilter getFilter() {
        return filter;
    }

    @Override
    public synchronized void close() {
        if (out != null) {
            out.close();
            out = null;
        }
    }

    /**
     * Sets the log pattern.
     * @param pattern the log pattern string
     */
    public void setPattern(String pattern) {
        logPattern.setPattern(pattern);
    }

    /**
     * Gets the log pattern.
     * @return the log pattern string
     */
    public String getPattern() {
        return logPattern.getPattern();
    }
}
