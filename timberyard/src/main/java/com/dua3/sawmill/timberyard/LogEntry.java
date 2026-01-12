package com.dua3.sawmill.timberyard;

import org.slb4j.Location;
import org.slb4j.LogLevel;
import org.slb4j.MDC;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * The LogEntry record encapsulates information about a single log event.
 * It is an immutable and thread-safe representation of a log message
 * containing all relevant metadata associated with the log entry.
 *
 * @param time        The timestamp of when the log event occurred.
 * @param logger      The name of the logger that generated the log event.
 * @param level       The severity level of the log event, represented by {@link LogLevel}.
 * @param marker      An optional marker associated with the log event, providing additional categorization.
 * @param mdc         An optional Mapping Diagnostic Context (MDC) associated with the log event, providing
 *                    contextual information for diagnostic purposes.
 * @param location    An optional {@link Location} object providing details about the origin of the log event
 *                    within the code (e.g., class name, method name, file name, line number).
 * @param message     A supplier of the log message. The message is lazily evaluated, allowing computation
 *                    to be deferred until it is actually needed.
 * @param throwable   An optional throwable associated with the log event, representing an exception or error
 *                    related to the event.
 */
public record LogEntry(
        Instant time,
        String logger,
        LogLevel level,
        @Nullable String marker,
        @Nullable MDC mdc,
        @Nullable Location location,
        @Nullable String message,
        @Nullable Throwable throwable
) {
}
