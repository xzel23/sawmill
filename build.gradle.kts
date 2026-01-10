plugins {
    id("java-library")
    id("com.dua3.cabe") version "3.1.0"
}

group = "com.dua3.lumberjack"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
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
