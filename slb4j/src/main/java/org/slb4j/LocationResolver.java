package org.slb4j;

import org.jspecify.annotations.Nullable;

public interface LocationResolver {
    @Nullable Location resolve();
}
