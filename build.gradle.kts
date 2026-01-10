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
plugins {
    id("java-library")
    id("com.dua3.gradle.jdkprovider") version "0.4.0"
    id("com.dua3.cabe") version "3.1.0"
}

group = "com.dua3.lumberjack"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

jdk {
    version = 21
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")

    compileOnly(platform("org.apache.logging.log4j:log4j-bom:2.25.3"))
    compileOnly("org.apache.logging.log4j:log4j-api")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("commons-logging:commons-logging:1.3.5")

    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
