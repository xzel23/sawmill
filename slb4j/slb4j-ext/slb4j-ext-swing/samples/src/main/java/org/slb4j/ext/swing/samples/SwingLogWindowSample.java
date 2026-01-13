package org.slb4j.ext.swing.samples;

import org.slb4j.SLB4J;
import org.slb4j.ext.swing.SwingLogWindow;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * This class demonstrates the use of {@link SwingLogWindow} to display log messages.
 */
public class SwingLogWindowSample {

    static {
        SLB4J.init();
    }

    private static final int AVERAGE_SLEEP_MILLIS = 50;
    private static final int LOG_BUFFER_SIZE = 10_000;
    private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(SwingLogWindowSample.class);
    private final SwingLogWindow logWindow;
    private final SecureRandom random = new SecureRandom();
    private final AtomicInteger n = new AtomicInteger();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingLogWindowSample sample = new SwingLogWindowSample();
            sample.start();
        });
    }

    public SwingLogWindowSample() {
        logWindow = new SwingLogWindow("Swing Log Viewer Sample", LOG_BUFFER_SIZE);
    }

    public void start() {
        logWindow.setVisible(true);
        startLoggingThreads();
    }

    private void startLoggingThreads() {
        final int numberOfThreads = 2;
        for (int i = 0; i < numberOfThreads; i++) {
            int threadId = i;
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(random.nextInt(2 * AVERAGE_SLEEP_MILLIS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    int nr = n.incrementAndGet();
                    int level = random.nextInt(5);
                    String msg = "Message #" + nr + " from thread " + threadId;

                    switch (level) {
                        case 0 -> SLF4J_LOGGER.trace(msg);
                        case 1 -> SLF4J_LOGGER.debug(msg);
                        case 2 -> SLF4J_LOGGER.info(msg);
                        case 3 -> SLF4J_LOGGER.warn(msg);
                        case 4 -> SLF4J_LOGGER.error(msg, new RuntimeException("Sample exception"));
                    }
                }
            }, "Logger-Thread-" + i);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
