plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "io.github.jhaago.sealdashboard"
    compileSdk = 37
    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "io.github.jhaago.sealdashboard"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-mock"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = true }
}
dependencies {
    implementation(project(":vehicle-core"))
    implementation(project(":vehicle-mock"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.tooling)
    testImplementation(libs.junit)
}
