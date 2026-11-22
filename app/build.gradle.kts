plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    jacoco // reporte de cobertura de las pruebas unitarias
}

android {
    namespace = "cl.duoc.mpazneb"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.mpazneb"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // FIRMA DEL APK: llave (.jks) generada para la app. Se deja en el repositorio porque la pauta lo pide.
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/mpazneb-release.jks")
            storePassword = "Mpaz2026Release"
            keyAlias = "mpazneb"
            keyPassword = "Mpaz2026Release"
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true // JaCoCo mide qué líneas ejecutan las pruebas
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose y AndroidX
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navegación con rutas tipadas
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Persistencia local (SQLite con Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Red: Retrofit (microservicio propio + API externa)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Pruebas unitarias (Guía 15): Kotest + JUnit 5 + MockK + corrutinas
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)                  // JUnit 4: pruebas de la EP2 (LogicaTest)
    testRuntimeOnly(libs.junit.vintage.engine)      // permite correr JUnit 4 sobre la plataforma JUnit 5
    testRuntimeOnly(libs.junit.platform.launcher)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core) // versión nueva: compatible con Android 15+
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

/**
 * Reporte de cobertura de la LÓGICA de la app (repositorios, validaciones, modelo y ViewModel de feriados).
 * Se ejecuta con: Gradle > app > Tasks > verification > reporteCobertura
 * Resultado: app/build/reports/jacoco/reporteCobertura/html/index.html
 */
tasks.register<JacocoReport>("reporteCobertura") {
    group = "verification"
    description = "Ejecuta las pruebas unitarias y genera el reporte de cobertura de la lógica."
    dependsOn("testDebugUnitTest")

    val logica = listOf(
        "cl/duoc/mpazneb/data/repository/**",
        "cl/duoc/mpazneb/data/remote/ErroresKt*",
        "cl/duoc/mpazneb/validacion/**",
        "cl/duoc/mpazneb/model/**",
        "cl/duoc/mpazneb/viewmodel/FeriadosViewModel*",
        "cl/duoc/mpazneb/viewmodel/FeriadosUiState*"
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) { include(logica) }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/**/*.exec") })
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

// Obligatorio para usar JUnit 5 / Kotest (Guía 15). Además, cada vez que se ejecutan
// las pruebas unitarias se genera el reporte de cobertura.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy("reporteCobertura")
}
