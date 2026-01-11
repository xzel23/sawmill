plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.3"
}

description = "Lumberjack performance benchmarks"

dependencies {
    implementation(project(":lumberjack"))
    
    // JMH
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    
    // Frontends
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    implementation("commons-logging:commons-logging:1.3.5")
    
    // Core dependencies needed for all benchmarks compilation
    implementation("org.apache.logging.log4j:log4j-core:2.25.3")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    
    jmh("org.apache.logging.log4j:log4j-core:2.25.3")
    jmh("ch.qos.logback:logback-classic:1.5.16")
    jmh(project(":lumberjack"))

    // Backends - runtime bridges
    val backend = project.findProperty("backend")?.toString() ?: "lumberjack"
    
    when (backend) {
        "log4j" -> {
            jmh("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.3") // slf4j to log4j
            jmh("org.apache.logging.log4j:log4j-jcl:2.25.3") // jcl to log4j
            jmh("org.apache.logging.log4j:log4j-jul:2.25.3") // jul to log4j
        }
        "logback" -> {
            jmh("org.slf4j:jcl-over-slf4j:2.0.17") // jcl to slf4j
            jmh("org.slf4j:jul-to-slf4j:2.0.17") // jul to slf4j
            jmh("org.apache.logging.log4j:log4j-to-slf4j:2.25.3") // log4j to slf4j
        }
        "jul" -> {
            jmh("org.slf4j:slf4j-jdk14:2.0.17") // slf4j to jul
            // JCL to JUL is handled by JCL's default behavior or system properties
        }
        "lumberjack" -> {
            // Lumberjack handles all four frontends directly, no bridges needed
        }
    }
}

tasks.compileJava {
    dependsOn(":lumberjack:cabe")
}

jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
    resultFormat.set("JSON")
    jvmArgs.set(listOf("-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"))
}
