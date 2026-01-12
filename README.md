Project Sawmill
===============

- **Lumberjack**: A lightweight, zero-dependencies logging backend for Java.
- **Timberyard**: Make Lumberjack's output available for further processing, for example implements a LogBuffer.
- **Carpenter**: Builds on Timberland, provides UI-Elements for live monitoring log messages.

All projects use Java 21 as baseline.

Lumberjack
----------

Lumberjack is a lightweight, zero-dependencies logging backend for Java.

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
- Add the single Lumberjack dependency to your code.
- Add a single line of code that executes at application startup and initializes the backend.
- Provide a logging.properties file to configure the logging backend. (optional)

### Configuration

TODO describe format of logging.properties and runtime configuration.

### Performance

TODO: Implement and benchmarks and add results for lumberjack and other backends here.
