package org.slb4j.ext.fx;

public interface LogPaneTexts {
    String textSearchUp();

    String textSearchDown();

    String textClear();

    String textLogLevel();

    String textLogger();

    String textLogText();

    String textSearch();

    String textColumnTime();

    String textColumnLevel();

    String textColumnLogger();

    String textColumnMessage();

    static LogPaneTexts of(
            String textSearchUp,
            String textSearchDown,
            String textClear,
            String textLogLevel,
            String textLogger,
            String textLogText,
            String textSearch,
            String textColumnTime,
            String textColumnLevel,
            String textColumnLogger,
            String textColumnMessage
    ) {
        return new LogPaneTextsRecord(textSearchUp, textSearchDown, textClear, textLogLevel, textLogger, textLogText, textSearch, textColumnTime, textColumnLevel, textColumnLogger, textColumnMessage);
    }
}

record LogPaneTextsRecord(
        String textSearchUp,
        String textSearchDown,
        String textClear,
        String textLogLevel,
        String textLogger,
        String textLogText,
        String textSearch,
        String textColumnTime,
        String textColumnLevel,
        String textColumnLogger,
        String textColumnMessage
) implements LogPaneTexts {
}
