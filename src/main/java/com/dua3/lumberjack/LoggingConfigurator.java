package com.dua3.lumberjack;

import java.util.Properties;
import java.util.function.Consumer;

public final class LoggingConfigurator {

    public static void configure(Properties properties, Consumer<LogFilter> setRootFilter, Consumer<LogHandler> addHandler) {
        LoggingConfiguration config = LoggingConfiguration.parse(properties);

        config.getHandlers().forEach(addHandler);
        setRootFilter.accept(config.getRootFilter());
    }
}
