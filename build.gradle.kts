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

    implementation ("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation ("org.apache.logging.log4j:log4j-core:2.20.0")


    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.apache.pdfbox:pdfbox:2.0.27")



    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")

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