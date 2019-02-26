plugins {
    kotlin("jvm")
}

kotlinProject()

dataLibs()

dependencies {
    implementation("org.quartz-scheduler:quartz:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    implementation(project(":pleo-antaeus-data"))
    compile(project(":pleo-antaeus-models"))
}