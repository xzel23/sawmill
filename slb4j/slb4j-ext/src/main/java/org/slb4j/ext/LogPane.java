package org.slb4j.ext;

/**
 * Common interface for log pane components.
 */
public interface LogPane {
    /**
     * Retrieves the LogBuffer associated with this LogPane.
     *
     * @return the LogBuffer instance used by this LogPane
     */
    LogBuffer getLogBuffer();
}
