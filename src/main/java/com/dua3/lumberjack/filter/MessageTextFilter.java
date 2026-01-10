package com.dua3.lumberjack.filter;

import com.dua3.lumberjack.LogFilter;
import com.dua3.lumberjack.LogLevel;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The DefaultLogEntryFilter class is an implementation of the LogEntryFilter interface
 * that filters log entries based on their log level and a user-defined filter.
 *
 * <p>DefaultLogEntryFilter provides methods to set and retrieve the log level and filter,
 * as well as a test method to determine if a LogEntry should be included or excluded.
 */
public final class MessageTextFilter implements LogFilter {

    private final String name;
    private final Predicate<? super String> textFilter;

    /**
     * Constructs a new DefaultLogEntryFilter with the specified log level and filter.
     *
     * @param name  the name of this filter
     * @param textFilter the filter to set for the message content
     */
    public MessageTextFilter(String name, Predicate<? super String> textFilter) {
        this.name = name;
        this.textFilter = textFilter;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        return textFilter.test(msg.get());
    }

    @Override
    public boolean isEnabled(String loggerName, LogLevel logLevel, @Nullable String marker) {
        return true;
    }
}
