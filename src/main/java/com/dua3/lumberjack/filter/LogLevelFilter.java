package com.dua3.lumberjack.filter;

import com.dua3.lumberjack.LogFilter;
import com.dua3.lumberjack.LogLevel;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * The DefaultLogEntryFilter class is an implementation of the LogEntryFilter interface
 * that filters log entries based on their log level and a user-defined filter.
 *
 * <p>DefaultLogEntryFilter provides methods to set and retrieve the log level and filter,
 * as well as a test method to determine if a LogEntry should be included or excluded.
 */
public final class LogLevelFilter implements LogFilter {

    private final String name;
    private final LogLevel level;

    /**
     * Constructs a new DefaultLogEntryFilter with the specified log level and filter.
     *
     * @param name  the name of the filter
     * @param level the minimal level to let through
     */
    public LogLevelFilter(String name, LogLevel level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        return lvl.ordinal() >= level.ordinal();
    }

    @Override
    public boolean isEnabled(String loggerName, LogLevel logLevel, @Nullable String marker) {
        return isLevelEnabled(logLevel);
    }

    @Override
    public boolean isLevelEnabled(LogLevel logLevel) {
        return logLevel.ordinal() >= level.ordinal();
    }
}
