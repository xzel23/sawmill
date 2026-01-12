plugins {
    application
}

dependencies {
    implementation(project(":carpenter:carpenter-fx"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("commons-logging:commons-logging:1.3.5")
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation("com.dua3.utility:utility")
    implementation("com.dua3.utility:utility-fx")
}

application {
    mainClass.set("com.dua3.sawmill.carpenter.fx.samples.Main")
}
