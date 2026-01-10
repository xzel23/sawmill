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
public final class MarkerFilter implements LogFilter {

    private final String name;
    private final Predicate<? super String> precicate;

    /**
     * Constructs a new DefaultLogEntryFilter with the specified log level and filter.
     *
     * @param name  the name of the filter
     * @param markerFilter the filter to set for the logger name
     */
    public MarkerFilter(String name, Predicate<? super String> markerFilter) {
        this.name = name;
        this.precicate = markerFilter;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        return precicate.test(mrk);
    }

    @Override
    public boolean isEnabled(String loggerName, LogLevel logLevel, @Nullable String marker) {
        return isMarkerEnabled(marker);
    }

    @Override
    public boolean isMarkerEnabled(@Nullable String marker) {
        return precicate.test(marker == null ? "" : marker);
    }
}
