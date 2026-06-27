plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "com.example.server"
version = "1.0.0"

application {
    mainClass.set("com.example.server.ApplicationKt")
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-gson:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")

    implementation("com.h2database:h2:2.2.224")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}
