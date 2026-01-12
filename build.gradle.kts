plugins {
    alias(libs.plugins.jdkprovider)
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
