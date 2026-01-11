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
    }

    configure<JavaApplication> {
        mainClass.set("com.dua3.sawmill.lumberjack.samples." + project.name + ".Main")
    }
}
