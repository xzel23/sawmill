package org.slb4j;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface LocationResolver {
    @Nullable Location resolve();
}
