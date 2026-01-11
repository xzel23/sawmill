subprojects {
    apply(plugin = "java")
    apply(plugin = "application")

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(project(":lumberjack"))
    }

    tasks.withType<JavaCompile> {
        dependsOn(":lumberjack:cabe")
        options.release.set(21)
    }

    configure<JavaApplication> {
        mainClass.set("com.dua3.sawmill.lumberjack.samples." + project.name + ".Main")
    }
}
