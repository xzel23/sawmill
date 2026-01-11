package com.dua3.lumberjack;

import org.jspecify.annotations.Nullable;

public interface LocationResolver {
    @Nullable Location resolve();
}
