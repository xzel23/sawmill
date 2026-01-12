plugins {
    application
}

dependencies {
    implementation(project(":carpenter:carpenter-swing"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("commons-logging:commons-logging:1.3.5")
}

application {
    mainClass.set("com.dua3.sawmill.carpenter.swing.samples.Main")
}
