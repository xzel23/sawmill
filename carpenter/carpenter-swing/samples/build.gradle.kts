plugins {
    application
}

dependencies {
    implementation(platform(libs.log4j.bom))
    implementation(project(":carpenter:carpenter-swing"))
    implementation(libs.slf4j.api)
    implementation(libs.log4j.api)
    implementation(libs.commons.logging)
}

application {
    mainClass.set("com.dua3.sawmill.carpenter.swing.samples.Main")
}
