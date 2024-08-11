plugins {
    kotlin("jvm") version "1.9.0"
}


allprojects{
    group = "org.example"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects{
    apply(plugin = "kotlin")

    dependencies {
        testImplementation(kotlin("test"))
    }

}




tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
