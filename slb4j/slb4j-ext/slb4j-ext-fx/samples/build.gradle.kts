plugins {
    application
}

dependencies {
    implementation(platform(libs.log4j.bom))
    implementation(project(":slb4j:slb4j-ext"))
    implementation(project(":slb4j:slb4j-ext:slb4j-ext-fx"))
    implementation(libs.slf4j.api)
    implementation(libs.log4j.api)
    implementation(libs.commons.logging)
}

application {
    mainClass.set("com.dua3.sawmill.carpenter.fx.samples.Main")
}
