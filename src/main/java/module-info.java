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

import org.jspecify.annotations.NullMarked;

/**
 * Module-info for the Lumberjack logging backend library.
 */
@NullMarked
module lumberjack.logger {
    exports com.dua3.lumberjack;
    exports com.dua3.lumberjack.filter;
    exports com.dua3.lumberjack.handler;

    requires org.jspecify;

    requires static java.logging;
    requires static org.apache.commons.logging;
    requires static org.apache.logging.log4j;
    requires static org.slf4j;
    requires java.xml;
}
