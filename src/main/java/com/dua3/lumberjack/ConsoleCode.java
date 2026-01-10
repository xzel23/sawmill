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
