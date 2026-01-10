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
package com.dua3.lumberjack.support;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class Util {
    private Util() {
        // utility class, no instances
    }

    /**
     * Wraps a supplier of a string to defer execution of the string creation
     * until its actual usage. This can be useful in scenarios where the string
     * construction is expensive and may not always be needed.
     *
     * @param supplier a supplier that provides the string lazily when requested
     * @return an object that defers the evaluation of the supplier until its string representation is required
     */
    public static Supplier<String> cachingStringSupplier(Supplier<String> supplier) {
        return supplier instanceof CachingStringSupplier cs ? cs : new CachingStringSupplier(supplier);
    }

    private static final class CachingStringSupplier implements Supplier<String> {
        private final Supplier<String> supplier;
        private @Nullable String s;

        CachingStringSupplier(Supplier<String> supplier) {
            this.supplier = supplier;
            s = null;
        }

        @Override
        public String get() {
            return s != null ? s : (s = supplier.get());
        }

        @Override
        public String toString() {
            return get();
        }
    }
}
