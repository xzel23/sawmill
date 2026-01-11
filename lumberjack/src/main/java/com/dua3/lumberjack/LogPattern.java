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
package com.dua3.lumberjack;

import org.jspecify.annotations.Nullable;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The LogPattern class handles the formatting of log entries using Log4J-style format strings.
 */
public final class LogPattern {

    private static final String NEWLINE = System.lineSeparator();
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private static final ThreadLocal<StringBuilder> SB_THREAD_LOCAL = ThreadLocal.withInitial(() -> new StringBuilder(256));
    public static final String DEFAULT_PATTERN = "%Cstart%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger - %msg%Cend%n";

    /**
     * Defines an interface for formatting log entries in a customizable and extensible manner.
     * Implementations of this interface allow specific components of a log entry to be
     * processed and appended to a {@link StringBuilder} in a format defined by the implementing class.
     */
    private interface LogPatternEntry {
        void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes);

        String getLog4jPattern();
    }

    /**
     * An abstract representation of a log format entry, which specifies how individual
     * components of a log message are formatted. This class implements the {@link LogPatternEntry}
     * interface and provides foundational methods for formatting and alignment.
     * <p>
     * Subclasses must define the specific formatting behavior for different log components.
     */
    private abstract static class AbstractLogPatternEntry implements LogPatternEntry {
        protected final String prefix;
        protected final int minWidth;
        protected final int maxWidth;
        protected final boolean leftAlign;

        /**
         * Constructs an instance of the AbstractLogPatternEntry with the specified
         * formatting parameters.
         *
         * @param prefix The prefix string that identifies the log component. This
         *               string will be included in the formatted log output.
         * @param minWidth The minimum width of the formatted output. If the formatted
         *                 log component is shorter than this width, padding will be
         *                 added to meet the minimum length.
         * @param maxWidth The maximum width of the formatted output. If the formatted
         *                 log component is longer than this width, it will be truncated
         *                 to the specified maximum length.
         * @param leftAlign A flag indicating whether the formatted output should be
         *                  left-aligned. If true, padding will be added to the right
         *                  of the formatted output; otherwise, padding will be added
         *                  to the left.
         */
        protected AbstractLogPatternEntry(String prefix, int minWidth, int maxWidth, boolean leftAlign) {
            this.prefix = prefix;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.leftAlign = leftAlign;
        }

        private static final int N_SPACES = 20;
        private static final String SPACES = " ".repeat(N_SPACES);

        /**
         * Appends a specified number of spaces to the given StringBuilder.
         *
         * @param sb the StringBuilder to which spaces will be appended
         * @param n the number of spaces to append
         */
        private static void appendSpaces(StringBuilder sb, int n) {
            while (n > 0) {
                int count = Math.min(n, SPACES.length());
                sb.append(SPACES, 0, count);
                n -= count;
            }
        }

        /**
         * Appends a formatted string value to the provided {@code StringBuilder}. The method applies
         * formatting rules such as truncation and alignment based on the class fields {@code minWidth},
         * {@code maxWidth}, and {@code leftAlign}. It delegates to another version of the same method,
         * enabling additional control over left truncation.
         *
         * @param sb the {@code StringBuilder} to which the formatted value will be appended
         * @param value the string value to format and append; if {@code null}, it will be treated as an empty string
         */
        protected void appendFormatted(StringBuilder sb, @Nullable String value) {
            appendFormatted(sb, value, false);
        }

        /**
         * Appends a formatted string value to the provided {@code StringBuilder}, applying
         * formatting rules such as truncation, padding, and alignment based on the
         * specified class fields {@code minWidth}, {@code maxWidth}, and {@code leftAlign}.
         * This method provides additional control over truncation direction.
         * <p>
         * If the string value exceeds the maximum width, it will either be truncated
         * from the left or the right, depending on the {@code leftTruncate} parameter.
         * If the string value is shorter than the minimum width, padding will be added
         * on either the left or right side, as determined by the alignment settings.
         *
         * @param sb the {@code StringBuilder} to which the formatted value will be appended
         * @param value the string value to format and append; if {@code null}, it will be treated as an empty string
         * @param leftTruncate a flag indicating whether to truncate the string from the left when its length exceeds the maximum width
         */
        protected void appendFormatted(StringBuilder sb, @Nullable String value, boolean leftTruncate) {
            if (value == null) {
                value = "";
            }

            if (maxWidth > 0 && value.length() > maxWidth) {
                if (leftTruncate) {
                    sb.append(value, value.length() - maxWidth, value.length());
                } else {
                    sb.append(value, 0, maxWidth);
                }
                return;
            }

            if (value.length() < minWidth) {
                int padding = minWidth - value.length();
                if (leftAlign) {
                    sb.append(value);
                    appendSpaces(sb, padding);
                } else {
                    appendSpaces(sb, padding);
                    sb.append(value);
                }
            } else {
                sb.append(value);
            }
        }

        @Override
        public String getLog4jPattern() {
            if (minWidth == 0 && maxWidth == 0) {
                return "%" + prefix;
            }
            StringBuilder sb = new StringBuilder("%");
            if (leftAlign) sb.append("-");
            if (minWidth > 0) sb.append(minWidth);
            if (maxWidth > 0) sb.append(".").append(maxWidth);
            sb.append(prefix);
            return sb.toString();
        }
    }

    /**
     * Represents a literal string entry in a log format.
     */
    private static class LiteralEntry implements LogPatternEntry {
        private final String literal;

        /**
         * Creates a new instance of LiteralEntry with the specified literal.
         *
         * @param literal the fixed string that this entry represents;
         *                it will be appended during log formatting.
         */
        LiteralEntry(String literal) {
            this.literal = literal;
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            sb.append(literal);
        }

        @Override
        public String getLog4jPattern() {
            return literal.replace("%", "%%");
        }
    }

    /**
     * Represents a specific log format entry for handling log levels within log messages.
     * <p>
     * Instances of this class are responsible for converting a {@code LogLevel} to a string
     * and applying formatting options such as alignment and truncation based on the
     * parent class's configuration.
     */
    private static class LevelEntry extends AbstractLogPatternEntry {
        LevelEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("p", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, lvl.name());
        }
    }

    /**
     * A specialized log format entry for formatting and appending logger names to log messages.
     * <p>
     * Instances of this class are responsible for appending the logger name to a {@code StringBuilder}
     * with formatting applied according to the specified minimum width, maximum width, and alignment
     * settings. If truncation is required, the logger name will be truncated from the left.
     */
    private static class LoggerEntry extends AbstractLogPatternEntry {
        private final int abbreviationLength;

        /**
         * Constructs an instance of LoggerEntry, a specialized log format entry
         * for formatting and appending logger names to log messages.
         *
         * @param minWidth the minimum width of the logger name in the formatted output.
         *                 If the logger name is shorter than this width, padding will
         *                 be added to meet the minimum length.
         * @param maxWidth the maximum width of the logger name in the formatted output.
         *                 If the logger name exceeds this width, it will be truncated
         *                 from the left to meet the specified maximum length.
         * @param leftAlign a flag indicating whether the logger name should be left-aligned.
         *                  If true, padding will be added to the right of the logger name;
         *                  otherwise, padding will be added to the left.
         * @param abbreviationLength the number of rightmost components of the logger name to keep.
         */
        LoggerEntry(int minWidth, int maxWidth, boolean leftAlign, int abbreviationLength) {
            super("c", minWidth, maxWidth, leftAlign);
            this.abbreviationLength = abbreviationLength;
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            String nameToAppend = loggerName;
            if (abbreviationLength > 0) {
                String[] parts = loggerName.split("\\.");
                if (parts.length > abbreviationLength) {
                    StringBuilder abbreviated = new StringBuilder();
                    for (int i = parts.length - abbreviationLength; i < parts.length; i++) {
                        if (abbreviated.length() > 0) {
                            abbreviated.append('.');
                        }
                        abbreviated.append(parts[i]);
                    }
                    nameToAppend = abbreviated.toString();
                }
            }
            appendFormatted(sb, nameToAppend, true);
        }

        @Override
        public String getLog4jPattern() {
            String format = super.getLog4jPattern();
            if (abbreviationLength > 0) {
                format = format.replace(prefix, prefix + "{" + abbreviationLength + "}");
            }
            return format;
        }
    }

    /**
     * Represents a log format entry for the thread name.
     */
    private static class ThreadEntry extends AbstractLogPatternEntry {
        ThreadEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("t", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, Thread.currentThread().getName());
        }
    }

    /**
     * Represents a log format entry for the MDC (Mapped Diagnostic Context).
     */
    private static class MdcEntry extends AbstractLogPatternEntry {
        private final @Nullable String key;

        MdcEntry(int minWidth, int maxWidth, boolean leftAlign, @Nullable String key) {
            super("X", minWidth, maxWidth, leftAlign);
            this.key = key;
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            if (mdc == null) {
                return;
            }
            if (key != null) {
                appendFormatted(sb, mdc.get(key));
            } else {
                StringBuilder mdcSb = new StringBuilder();
                mdc.stream().forEach(e -> {
                    if (mdcSb.length() > 0) {
                        mdcSb.append(", ");
                    }
                    mdcSb.append(e.getKey()).append("=").append(e.getValue());
                });
                appendFormatted(sb, mdcSb.toString());
            }
        }

        @Override
        public String getLog4jPattern() {
            String format = super.getLog4jPattern();
            if (key != null) {
                format = format.replace(prefix, prefix + "{" + key + "}");
            }
            return format;
        }
    }

    /**
     * Represents a log format entry that formats and appends a marker value to a log output.
     * A marker is a string that can be used in log messages to provide additional context or categorization.
     */
    private static class MarkerEntry extends AbstractLogPatternEntry {
        /**
         * Constructs an instance of MarkerEntry with the specified formatting parameters.
         * A MarkerEntry formats and appends a marker value to the log output. A marker is
         * a string that provides additional context or categorization for log messages.
         *
         * @param minWidth  the minimum width of the formatted marker output. If the marker
         *                  output is shorter than this width, padding will be added to meet
         *                  the minimum length.
         * @param maxWidth  the maximum width of the formatted marker output. If the marker
         *                  output is longer than this width, it will be truncated to the
         *                  specified maximum length.
         * @param leftAlign a flag indicating whether the marker output should be left-aligned.
         *                  If true, padding will be added to the right of the marker output;
         *                  otherwise, padding will be added to the left.
         */
        MarkerEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("marker", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, mrk);
        }
    }

    /**
     * Represents a specific implementation of {@code AbstractLogPatternEntry} for formatting
     * log message content in a log entry. This class is responsible for formatting the
     * log message text according to the provided parameters for minimum width, maximum width,
     * and alignment.
     */
    private static class MessageEntry extends AbstractLogPatternEntry {
        /**
         * Constructs a new instance of MessageEntry with the specified formatting parameters.
         *
         * @param minWidth  the minimum width of the formatted output. If the output is shorter
         *                  than this width, padding will be added to meet the minimum length.
         * @param maxWidth  the maximum width of the formatted output. If the output is longer
         *                  than this width, it will be truncated to fit the specified maximum length.
         * @param leftAlign a flag indicating if the formatted output should be left-aligned. When set
         *                  to true, padding is added to the right of the output; otherwise, it is
         *                  added to the left.
         */
        MessageEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("m", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, msg.get());
        }
    }

    /**
     * Represents a log format entry for displaying location information within log messages.
     * <p>
     * This class formats the location string according to specified width and alignment constraints.
     */
    private static class LocationEntry extends AbstractLogPatternEntry {
        /**
         * Constructs an instance of the LocationEntry class. This constructor initializes
         * a log format entry responsible for formatting and displaying the location
         * information in log messages, with the specified formatting parameters.
         *
         * @param minWidth The minimum width of the formatted location output. If the location
         *                 string is shorter than this width, padding will be added to meet the
         *                 minimum length.
         * @param maxWidth The maximum width of the formatted location output. If the location
         *                 string is longer than this width, it will be truncated to the specified
         *                 maximum length.
         * @param leftAlign A flag indicating whether the formatted location output should be
         *                  left-aligned. If true, padding will be added to the right of the
         *                  formatted output; otherwise, padding will be added to the left.
         */
        LocationEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("l", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, location);
        }
    }

    /**
     * The ExceptionEntry class is a specialized implementation of the AbstractLogPatternEntry
     * for handling and formatting exception-related log entries. It formats the exception information
     * into the log output, including the exception type and message.
     */
    private static class ExceptionEntry extends AbstractLogPatternEntry {
        /**
         * Constructs an instance of ExceptionEntry, a specialized log format entry
         * that handles the formatting of exceptions in a logging framework. This
         * entry utilizes the specified parameters to format exception-related log output.
         *
         * @param minWidth the minimum width of the formatted output. If the output is
         *                 shorter than this width, padding will be applied to meet
         *                 the minimum length.
         * @param maxWidth the maximum width of the formatted output. If the output
         *                 exceeds this width, it will be truncated to conform to this
         *                 limit.
         * @param leftAlign a flag indicating whether the formatted output should be
         *                  left-aligned. If true, the padding will be added to the
         *                  right of the output; otherwise, padding will be applied
         *                  to the left.
         */
        ExceptionEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("ex", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            if (t != null) {
                appendFormatted(sb, t.getClass().getName() + ": " + t.getMessage());
                sb.append(NEWLINE);
            }
        }
    }

    /**
     * Represents a log format entry that injects the start of a color code into log formatting.
     * This is used to include terminal color codes in the log messages for enhanced visualization.
     * The color code to be inserted is specified via a pair of color codes passed as a parameter
     * during formatting.
     */
    private static class ColorStartEntry extends AbstractLogPatternEntry {
        /**
         * Constructs an instance of ColorStartEntry with the specified formatting parameters.
         *
         * @param minWidth the minimum width of the formatted output. If the formatted
         *                 log component is shorter than this width, padding will be
         *                 added to meet the minimum length.
         * @param maxWidth the maximum width of the formatted output. If the formatted
         *                 log component is longer than this width, it will be truncated
         *                 to the specified maximum length.
         * @param leftAlign a flag indicating whether the formatted output should be
         *                  left-aligned. If true, padding will be added to the right
         *                  of the formatted output; otherwise, padding will be added
         *                  to the left.
         */
        ColorStartEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("Cstart", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, consoleCodes == null ? "" : consoleCodes.start());
        }
    }

    /**
     * Represents a specific type of log format entry designed to insert an ending
     * color code into a log message.
     */
    private static class ColorEndEntry extends AbstractLogPatternEntry {
        /**
         * Constructs a ColorEndEntry instance used for formatting log entries with
         * specific width constraints and alignment settings.
         *
         * @param minWidth the minimum width of the formatted output. If the formatted
         *                 log component is shorter than this width, padding will be
         *                 added to meet the minimum length.
         * @param maxWidth the maximum width of the formatted output. If the formatted
         *                 log component is longer than this width, it will be truncated
         *                 to the specified maximum length.
         * @param leftAlign a flag indicating whether the formatted output should be
         *                  left-aligned. If true, padding will be added to the right
         *                  of the formatted output; otherwise, padding will be added
         *                  to the left.
         */
        ColorEndEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("Cend", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            appendFormatted(sb, consoleCodes == null ? "" : consoleCodes.end());
        }
    }

    /**
     * A class that formats date-time values for log entries using a specified pattern.
     * This class implements the {@code LogPatternEntry} interface and provides functionality
     * to format a log entry's timestamp according to various date-time patterns.
     */
    private static class DateEntry implements LogPatternEntry {
        private final String pattern;
        private final DateTimeFormatter formatter;

        /**
         * Constructs a {@code DateEntry} instance with the specified date-time pattern.
         *
         * @param pattern the pattern to be used for formatting date-time values;
         *                supported patterns include "ISO8601", "HH:mm:ss,SSS",
         *                "yyyy-MM-dd HH:mm:ss,SSS", "yyyy-MM-dd HH:mm:ss", or a custom pattern.
         *                If the pattern is empty, "HH:mm:ss" will be used as the default.
         */
        DateEntry(String pattern) {
            this.pattern = pattern;
            this.formatter = switch (pattern) {
                case "ISO8601" -> DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssSSSZ");
                case "HH:mm:ss,SSS" -> DateTimeFormatter.ofPattern("HH:mm:ss,SSS");
                case "yyyy-MM-dd HH:mm:ss,SSS" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
                case "yyyy-MM-dd HH:mm:ss" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                default -> DateTimeFormatter.ofPattern(pattern.isEmpty() ? "HH:mm:ss" : pattern);
            };
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            formatter.formatTo(instant.atZone(ZONE_ID), sb);
        }

        @Override
        public String getLog4jPattern() {
            return pattern.isEmpty() ? "%d" : "%d{" + pattern + "}";
        }
    }

    /**
     * Represents a log format entry that inserts a newline character into the log output.
     */
    private static class NewlineEntry implements LogPatternEntry {
        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
            sb.append(NEWLINE);
        }

        @Override
        public String getLog4jPattern() {
            return "%n";
        }
    }

    private volatile List<LogPatternEntry> entries;

    /**
     * Constructs a LogPattern using the default pattern.
     */
    public LogPattern() {
        setPattern(DEFAULT_PATTERN);
    }

    /**
     * Constructs a LogPattern using the supplied pattern.
     *
     * @param pattern the format pattern in Log4J style, which may include placeholders and literals
     */
    public LogPattern(String pattern) {
        setPattern(pattern);
    }

    /**
     * Set the format pattern.
     * @param pattern the format pattern in Log4J style
     */
    public void setPattern(String pattern) {
        this.entries = parseLog4jPatternString(pattern);
    }

    /**
     * Get the format pattern.
     * @return the format pattern in Log4J style
     */
    public String getPattern() {
        StringBuilder sb = new StringBuilder();
        for (LogPatternEntry entry : entries) {
            sb.append(entry.getLog4jPattern());
        }
        return sb.toString();
    }

    /**
     * Formats a log entry.
     *
     * @param out          the {@link PrintStream} to write the formatted log entry to
     * @param instant      the timestamp of the log entry
     * @param loggerName   the name of the logger
     * @param lvl          the log level
     * @param mrk          the marker
     * @param msg          the message supplier
     * @param mdc          the MDC context
     * @param location     the location information
     * @param t            the throwable, if any
     * @param consoleCodes the color codes for the log level (start and end)
     */
    public void formatLogEntry(PrintStream out, Instant instant, String loggerName, LogLevel lvl, @Nullable String mrk, @Nullable MDC mdc, Supplier<String> msg, String location, @Nullable Throwable t, @Nullable ConsoleCode consoleCodes) {
        StringBuilder sb = SB_THREAD_LOCAL.get();
        sb.setLength(0);
        for (LogPatternEntry entry : entries) {
            entry.format(sb, instant, loggerName, lvl, mrk, mdc, msg, location, t, consoleCodes);
        }
        out.print(sb);
        if (sb.length() > 4096) {
            sb.setLength(256);
            sb.trimToSize();
        }
    }

    private static final Pattern PATTERN = Pattern.compile("%(-?\\d*)(\\.\\d+)?([a-zA-Z]+)(\\{([^}]+)})?|%%|%n");

    /**
     * Parses a Log4J-style pattern string and converts it into a list of {@code LogPatternEntry} instances,
     * which can be used to format log entries according to the specified pattern.
     * <p>
     * Supported pattern specifiers include literals, placeholders for log levels, messages, loggers, etc.,
     * as well as specific options for alignment and truncation.
     *
     * @param pattern the pattern string in Log4J style, which may include placeholders and literals
     * @return a list of {@code LogPatternEntry} instances representing the parsed pattern
     */
    private static List<LogPatternEntry> parseLog4jPatternString(String pattern) {
        List<LogPatternEntry> entries = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(pattern);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                entries.add(new LiteralEntry(pattern.substring(lastEnd, matcher.start())));
            }

            String match = matcher.group();
            if (match.equals("%%")) {
                entries.add(new LiteralEntry("%"));
            } else if (match.equals("%n")) {
                entries.add(new NewlineEntry());
            } else {
                String minWidthStr = matcher.group(1);
                String maxWidthStr = matcher.group(2);
                String type = matcher.group(3);
                String options = matcher.group(5);

                boolean leftAlign = minWidthStr != null && minWidthStr.startsWith("-");
                int minWidth = (minWidthStr != null && !minWidthStr.isEmpty()) ? Math.abs(Integer.parseInt(minWidthStr)) : 0;
                int maxWidth = (maxWidthStr != null && maxWidthStr.length() > 1) ? Integer.parseInt(maxWidthStr.substring(1)) : 0;

                switch (type) {
                    case "p", "level" -> entries.add(new LevelEntry(minWidth, maxWidth, leftAlign));
                    case "c", "logger" -> {
                        int abbreviationLength = 0;
                        if (options != null && options.matches("\\d+")) {
                            abbreviationLength = Integer.parseInt(options);
                        }
                        entries.add(new LoggerEntry(minWidth, maxWidth, leftAlign, abbreviationLength));
                    }
                    case "marker" -> entries.add(new MarkerEntry(minWidth, maxWidth, leftAlign));
                    case "m", "msg", "message" -> entries.add(new MessageEntry(minWidth, maxWidth, leftAlign));
                    case "l", "location" -> entries.add(new LocationEntry(minWidth, maxWidth, leftAlign));
                    case "t", "thread" -> entries.add(new ThreadEntry(minWidth, maxWidth, leftAlign));
                    case "X", "mdc" -> entries.add(new MdcEntry(minWidth, maxWidth, leftAlign, options));
                    case "ex", "exception", "throwable" ->
                            entries.add(new ExceptionEntry(minWidth, maxWidth, leftAlign));
                    case "Cstart" -> entries.add(new ColorStartEntry(minWidth, maxWidth, leftAlign));
                    case "Cend" -> entries.add(new ColorEndEntry(minWidth, maxWidth, leftAlign));
                    case "d" -> entries.add(new DateEntry(options != null ? options : ""));
                    default -> entries.add(new LiteralEntry(match));
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < pattern.length()) {
            entries.add(new LiteralEntry(pattern.substring(lastEnd)));
        }
        return entries;
    }
}
