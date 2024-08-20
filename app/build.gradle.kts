plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("C:/Users/espin/Documents/my-release-key.jks")
        }
        create("main_share") {
            storeFile = file("C:\\Users\\espin\\Documents\\my-release-key.jks")
            storePassword = "AEedam1020budder!"
            keyAlias = "key0"
            keyPassword = "Edam1020!"
        }
    }
    namespace = "com.example.rhythmtapgame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rhythmtapgame"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("main_share")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("main_share")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("main_share")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.appcompat.v120)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.commons.csv)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.games.v2)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



}