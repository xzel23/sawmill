package com.dua3.lumberjack;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogPatternTest {

    @ParameterizedTest(name = "[{index}] pattern=\"{0}\"")
    @CsvSource(
            delimiter = '|',
            ignoreLeadingAndTrailingWhitespace = false,
            value = {
                    // Core patterns
                    "%msg%n|'Order 4711 processed\n'",
                    "%level %msg%n|'INFO Order 4711 processed\n'",
                    "%-5level %msg%n|'INFO  Order 4711 processed\n'",

                    // Timestamp patterns
                    "%d{yyyy-MM-dd HH:mm:ss.SSS} %msg%n|'2026-01-10 14:23:41.123 Order 4711 processed\n'",
                    "%d{HH:mm:ss.SSS} %msg%n|'14:23:41.123 Order 4711 processed\n'",

                    // Logger name patterning
                    "%logger %msg%n|'com.example.service.OrderService Order 4711 processed\n'",
                    "%logger{1} %msg%n|'OrderService Order 4711 processed\n'",
                    "%logger{2} %msg%n|'service.OrderService Order 4711 processed\n'",

                    // Thread & context
                    "%t %msg%n|'main Order 4711 processed\n'",
                    "%X{userId} %msg%n|'alice Order 4711 processed\n'",
                    "%X{missing} %msg%n|' Order 4711 processed\n'",

                    // Marker
                    "%marker %msg%n|'AUDIT Order 4711 processed\n'",
                    "[%marker] %msg%n|'[AUDIT] Order 4711 processed\n'",

                    // Escaping & literals
                    "%% %msg%n|'% Order 4711 processed\n'",
                    "\"%msg\"%n|'\"Order 4711 processed\"\n'",

                    // Composite
                    "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{2} [%marker] %X{userId} - %msg%n"
                            + "|'2026-01-10 14:23:41.123 [main] INFO  service.OrderService [AUDIT] alice - Order 4711 processed\n'"
            }
    )
    void testPattern(String pattern, String expected) {
        String threadName = Thread.currentThread().getName();
        String updatedExpected = expected.replace("main", threadName);
        LogPattern fmt = new LogPattern(pattern);

        Instant instant = LocalDateTime.of(2026, 1, 10, 14, 23, 41, 123_000_000)
                .atZone(ZoneId.systemDefault())
                .toInstant();
        String loggerName = "com.example.service.OrderService";
        LogLevel level = LogLevel.INFO;
        String marker = "AUDIT";
        MDC mdc = new MDC() {
            Map<String, String> map = Map.of("userId", "alice", "requestId", "req-123");

            @Override
            public @Nullable String get(String key) {
                return map.get(key);
            }

            @Override
            public Stream<Map.Entry<String, String>> stream() {
                return map.entrySet().stream();
            }
        };
        Supplier<String> msg = () -> "Order 4711 processed";
        String location = null;
        Throwable t = null;
        ConsoleCode consoleCodes = ConsoleCode.empty();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            fmt.formatLogEntry(out, instant, loggerName, level, marker, mdc, location, msg, t, consoleCodes);
        }

        String actual = baos.toString(StandardCharsets.UTF_8);

        assertEquals(updatedExpected, actual);
    }
}
