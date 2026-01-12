Project Sawmill
===============

- **SLB4J**: A lightweight, zero-dependencies logging backend for Java.
- **Timberyard**: Make Lumberjack's output available for further processing, for example implements a LogBuffer.
- **Carpenter**: Builds on Timberland, provides UI-Elements for live monitoring log messages.

All projects use Java 21 as baseline.

SLB4J (codename Lumberjack)
---------------------------

SLB4J is a lightweight, zero-dependencies logging backend for Java.

### Features

- zero dependencies
- Log4J pattern language
- colored console output (optional)
- log rotation
- hierarchical definition of log levels
- lazy message formatting, i.e., messages are only formatted when needed
- MDC
- Marker
- Location (supported for __all__ frontends)

### How to use

- __Do not__ include any other logging backend (log4j-core, logback, etc.)
- __Do not__ add any logging bridge handler
- Add the single SLB4J dependency to your code.
- Add `SLB4J.init()` in a static initializer block of the class containing your `main()` method.
- Provide a logging.properties file to configure the logging backend. (optional)

### Configuration

TODO describe format of logging.properties and runtime configuration.

### Performance

TODO: Implement and benchmarks and add results for slb4j and other backends here.
