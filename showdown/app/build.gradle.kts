plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.showdown"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.showdown"
        minSdk = 26
        targetSdk = 35
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

    implementation(libs.mapbox.android.core)

    implementation(libs.place.autocomplete.ndk27)
    implementation(libs.autofill.ndk27)
    implementation(libs.mapbox.search.android.ndk27)
    implementation(libs.mapbox.search.android.ui.ndk27)

    implementation(libs.android)
    implementation(libs.common)

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    implementation("androidx.room:room-runtime:${roomVersion}")
    implementation(libs.play.services.maps)
    annotationProcessor("androidx.room:room-compiler:${roomVersion}")
    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}