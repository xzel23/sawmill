import org.jspecify.annotations.NullMarked;

/**
 * Module-info for the Lumberjack logging backend library.
 */
@NullMarked
module lumberjack.logger {
    exports com.dua3.lumberjack;
    exports com.dua3.lumberjack.filter;

    requires org.jspecify;

    requires static java.logging;
    requires static org.apache.commons.logging;
    requires static org.apache.logging.log4j;
    requires static org.slf4j;
}
