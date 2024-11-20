plugins {
    application
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application{
    mainClass.set("io.ktor.server.netty.EngineMain")
}


repositories {
    mavenCentral()
}

dependencies {

    implementation("org.ktorm:ktorm-core:3.5.0") // Hoặc phiên bản mới nhất
    implementation("org.ktorm:ktorm-support-mysql:3.5.0") // Nếu sử dụng MySQL

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0") // Thư viện hỗ trợ LocalDate



    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:2.2.3")
    implementation("io.ktor:ktor-server-netty:2.2.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.3")
    implementation("io.ktor:ktor-serialization-gson:2.2.3")

    // Logging dependency
    implementation("ch.qos.logback:logback-classic:1.2.6")


    // mySQL connector
    implementation("mysql:mysql-connector-java:8.0.33")

    // ktorm core
    implementation("org.ktorm:ktorm-core:3.4.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}