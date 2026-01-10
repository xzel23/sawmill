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
public final class LoggerNameFilter implements LogFilter {

    private final String name;
    private final Predicate<String> loggerNameFilter;

    /**
     * Constructs a new DefaultLogEntryFilter with the specified log level and filter.
     *
     * @param name  the name of the filter
     * @param loggerNameFilter the filter to set for the logger name
     */
    public LoggerNameFilter(String name, Predicate<String> loggerNameFilter) {
        this.name = name;
        this.loggerNameFilter = loggerNameFilter;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        return loggerNameFilter.test(loggerName);
    }

    @Override
    public boolean isEnabled(String loggerName, LogLevel logLevel, @Nullable String marker) {
        return loggerNameFilter.test(loggerName);
    }
}
