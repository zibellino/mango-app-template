import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val appProps = Properties().apply {
    load(FileInputStream(rootProject.file("app.properties")))
}
val appName: String = appProps.getProperty("app.name")
val appPackage: String = appProps.getProperty("app.package")

// versionCode/versionName are normally passed in from CI as project properties
// (-PappVersionCode=... -PappVersionName=...), derived from the run number and
// release tag respectively. Falls back to app.properties for local/dev builds
// where nobody passes those flags — versionCode 1 is fine locally since it's
// never uploaded anywhere.
val ciVersionCode = (project.findProperty("appVersionCode") as String?)?.toIntOrNull()
val ciVersionName = project.findProperty("appVersionName") as String?

android {
    namespace = appPackage
    compileSdk = 35

    defaultConfig {
        applicationId = appPackage
        minSdk = 26
        targetSdk = 35
        versionCode = ciVersionCode ?: 1
        versionName = ciVersionName ?: appProps.getProperty("app.version", "0.1.0")
        resValue("string", "app_name", appName)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    debugImplementation(libs.androidx.ui.tooling)
}
