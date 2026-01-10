import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

/*
 * Copyright 2026 Axel Howind - axh@dua3.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.dua3.gradle.jdkprovider") version "0.4.0"
    id("com.dua3.cabe") version "3.1.0"
    id("com.github.spotbugs") version "6.4.8"
}

/////////////////////////////////////////////////////////////////////////////
// Meta data object
/////////////////////////////////////////////////////////////////////////////

object Meta {
    const val VERSION = "0.1-SNAPSHOT"
    const val DESCRIPTION = "Lumberjack universal logging backend"
    const val INCEPTION_YEAR = "2026"
    const val GROUP = "com.dua3.lumberjack"
    const val SCM = "https://github.com/xzel23/lumberjack.git"
    const val LICENSE_NAME = "The Apache Software License, Version 2.0"
    const val LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}

group = Meta.GROUP
version = Meta.VERSION

// check for development/release version
fun isDevelopmentVersion(versionString: String): Boolean {
    val v = versionString.toDefaultLowerCase()
    val markers = listOf("snapshot", "alpha", "beta")
    return markers.any { marker -> v.contains("-$marker") || v.contains(".$marker") }
}

val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")

repositories {
    mavenCentral()
}

jdk {
    version = 21
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")

    compileOnly(platform("org.apache.logging.log4j:log4j-bom:2.25.3"))
    compileOnly("org.apache.logging.log4j:log4j-api")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("commons-logging:commons-logging:1.3.5")

    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

allprojects {
    // --- PUBLISHING ---

    if (pluginManager.hasPlugin("maven-publish")) {
        configure<PublishingExtension> {
            // Repositories for publishing
            repositories {
                // Sonatype snapshots for snapshot versions
                if (isSnapshot) {
                    maven {
                        name = "sonatypeSnapshots"
                        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                        credentials {
                            username = System.getenv("SONATYPE_USERNAME")
                            password = System.getenv("SONATYPE_PASSWORD")
                        }
                    }
                }

                // Always add root-level staging directory for JReleaser
                maven {
                    name = "stagingDirectory"
                    url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
                }
            }

            // Publications for non-BOM projects
            if (!project.name.endsWith("-bom")) {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])

                        groupId = Meta.GROUP
                        artifactId = project.name
                        version = project.version.toString()

                        pom {
                            name.set(project.name)
                            description.set(project.description)
                            url.set(Meta.SCM)

                            licenses {
                                license {
                                    name.set(Meta.LICENSE_NAME)
                                    url.set(Meta.LICENSE_URL)
                                }
                            }

                            developers {
                                developer {
                                    id.set(Meta.DEVELOPER_ID)
                                    name.set(Meta.DEVELOPER_NAME)
                                    email.set(Meta.DEVELOPER_EMAIL)
                                    organization.set(Meta.ORGANIZATION_NAME)
                                    organizationUrl.set(Meta.ORGANIZATION_URL)
                                }
                            }

                            scm {
                                connection.set("scm:git:${Meta.SCM}")
                                developerConnection.set("scm:git:${Meta.SCM}")
                                url.set(Meta.SCM)
                            }

                            withXml {
                                val root = asNode()
                                root.appendNode("inceptionYear", "2019")
                            }
                        }
                    }
                }
            }
        }
    }

    // Signing configuration deferred until after evaluation
    afterEvaluate {
        if (pluginManager.hasPlugin("signing")) {
            configure<SigningExtension> {
                val shouldSign = !project.version.toString().lowercase().contains("snapshot")
                setRequired(shouldSign && gradle.taskGraph.hasTask("publish"))

                val publishing = project.extensions.findByType<PublishingExtension>() ?: return@configure

                if (project.name.endsWith("-bom")) {
                    if (publishing.publications.names.contains("bomPublication")) {
                        sign(publishing.publications["bomPublication"])
                    }
                } else {
                    if (publishing.publications.names.contains("mavenJava")) {
                        sign(publishing.publications["mavenJava"])
                    }
                }
            }
        }
    }

    // set the project description after evaluation because it is not yet visible when the POM is first created
    afterEvaluate {
        if (pluginManager.hasPlugin("maven-publish")) {
            project.extensions.configure<PublishingExtension> {
                publications.withType<MavenPublication> {
                    pom {
                        if (description.orNull.isNullOrBlank()) {
                            description.set(project.description ?: "No description provided")
                        }
                    }
                }
            }
        }
    }

    // SpotBugs for non-BOM projects
    if (!project.name.endsWith("-bom") && pluginManager.hasPlugin("com.github.spotbugs")) {

        // === SPOTBUGS ===
        configure<com.github.spotbugs.snom.SpotBugsExtension> {
            excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsMain") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/main.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/test.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }
    }
}