// Microservicio de actividades y preguntas de "Camino de Estrellas" (Spring Boot + JPA + H2).
plugins {
    java
    id("org.springframework.boot") version "4.1.0"
}

group = "cl.duoc.mpazneb"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // BOM de Spring Boot: fija las versiones compatibles de todas las librerías de abajo
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")      // API REST
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")    // JPA / Hibernate
    implementation("org.springframework.boot:spring-boot-starter-validation")  // @NotBlank, @Min, ...
    implementation("org.springframework.boot:spring-boot-h2console")           // consola web de la base de datos
    runtimeOnly("com.h2database:h2")                                           // base de datos H2 (archivo local)
}
