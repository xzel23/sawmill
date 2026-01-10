package com.dua3.lumberjack;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Interface representing a Mapping Diagnostic Context (MDC).
 * <p>
 * An MDC is used to store contextual information that can be associated with log events
 * to provide additional context for diagnostic purposes. This interface provides a common
 * method to access information from different MDC providers.
 */
public interface MDC {
    /**
     * Retrieves the value associated with the specified key in the
     * Mapping Diagnostic Context (MDC).
     *
     * @param key the key used to retrieve the associated value.
     *            Must not be null.
     * @return the value associated with the specified key, or null if
     *         no such key exists in the MDC.
     */
    @Nullable String get(String key);

    Stream<Map.Entry<String, String>> stream();

    public static MDC empty() {
        return MDCConstants.EMPTY;
    }
}

class MDCConstants {
    static final MDC EMPTY = new MDC() {
        @Override
        public @Nullable String get(String key) {
            return null;
        }

        @Override
        public Stream<Map.Entry<String, String>> stream() {
            return Stream.empty();
        }
    };
}
