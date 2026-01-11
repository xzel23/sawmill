package com.dua3.sawmill.lumberjack;

import org.jspecify.annotations.Nullable;

public interface LocationResolver {
    @Nullable Location resolve();
}
