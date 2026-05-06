import org.gradle.kotlin.dsl.maven

rootProject.name = "market"

pluginManagement {
    repositories {
        maven {
            url = uri("https://artifactory.tcsbank.ru/artifactory/maven-all")
            name = "maven-all"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

include("market")
include("payments")
