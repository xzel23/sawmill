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

public final class ConsoleCode {

    public static final ConsoleCode EMPTY = new ConsoleCode("", "");

    private static final String ANSI_RESET = "\u001B[0m";

    private final String start;
    private final String end;

    public ConsoleCode(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public String start() {return start;}

    public String end() {return end;}

    public static ConsoleCode of(String start, String end) {
        return new ConsoleCode(start, end);
    }

    public static ConsoleCode ofAnsi(String start) {
        return new ConsoleCode(start, ANSI_RESET);
    }

    public static ConsoleCode empty() {
        return EMPTY;
    }
}
