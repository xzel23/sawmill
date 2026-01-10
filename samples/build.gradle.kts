subprojects {
    apply(plugin = "java")
    apply(plugin = "application")

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(project(":"))
    }

    tasks.withType<JavaCompile> {
        dependsOn(":cabe")
    }

    configure<JavaApplication> {
        mainClass.set("com.dua3.lumberjack.samples." + project.name + ".Main")
    }
}
