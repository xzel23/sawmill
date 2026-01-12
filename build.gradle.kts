plugins {
    id("com.dua3.gradle.jdkprovider") version "0.4.0"
}

allprojects {
    apply(plugin = "com.dua3.gradle.jdkprovider")

    jdk {
        version = 21
        javaFxBundled = true
    }

    repositories {
        mavenCentral()
    }
}
