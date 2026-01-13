plugins {
    application
}

dependencies {
    implementation(platform(libs.log4j.bom))
    implementation(platform(libs.utility.bom))
    implementation(project(":slb4j:slb4j-ext"))
    implementation(project(":slb4j:slb4j-ext:slb4j-ext-fx"))
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
