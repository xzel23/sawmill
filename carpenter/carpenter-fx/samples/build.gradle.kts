plugins {
    application
}

dependencies {
    implementation(platform(libs.log4j.bom))
    implementation(platform(libs.utility.bom))
    implementation(project(":carpenter:carpenter-fx"))
    implementation(libs.slf4j.api)
    implementation(libs.log4j.api)
    implementation(libs.commons.logging)
    implementation(libs.atlantafx)
    implementation(libs.utility)
    implementation(libs.utility.fx)
}

application {
    mainClass.set("com.dua3.sawmill.carpenter.fx.samples.Main")
}
