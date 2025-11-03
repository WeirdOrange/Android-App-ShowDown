plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.showdown"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.showdown"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val roomVersion = "2.6.1"

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common.jvm)
    implementation("androidx.room:room-runtime:${roomVersion}")
    implementation(libs.room.runtime.android)
    annotationProcessor("androidx.room:room-compiler:${roomVersion}")
    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}