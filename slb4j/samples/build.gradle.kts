subprojects {
    apply(plugin = "java")
    apply(plugin = "application")

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"(project(":slb4j"))
    }

    tasks.withType<JavaCompile> {
        dependsOn(":slb4j:cabe")
        options.release.set(21)
    }

    configure<JavaApplication> {
        mainClass.set("org.slb4j.samples." + project.name + ".Main")
    }
}
