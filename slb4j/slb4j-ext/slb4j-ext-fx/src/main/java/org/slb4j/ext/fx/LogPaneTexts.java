package org.slb4j.ext.fx;

public interface LogPaneTexts {
    String textSearchUp();

    String textSearchDown();

    String textClear();

    String labelFilterLogLevel();

    String labelFilterLogger();

    String labelFilterLogMessage();

    String labelSearchText();

    String headerTimeColumn();

    String headerLevelColumn();

    String headerLoggerColumn();

    String headerMessageColumn();

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
        String labelFilterLogLevel,
        String labelFilterLogger,
        String labelFilterLogMessage,
        String labelSearchText,
        String headerTimeColumn,
        String headerLevelColumn,
        String headerLoggerColumn,
        String headerMessageColumn
) implements LogPaneTexts {
}
