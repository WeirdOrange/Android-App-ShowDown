import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.showdown"
    compileSdk = 36

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.example.showdown"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //MAIL_ADDRESS and MAIL_APP_PASSWORD are saved in local.properties for security
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val mailAddress = localProperties.getProperty("MAIL_ADDRESS") ?: ""
        val mailPassword = localProperties.getProperty("MAIL_APP_PASSWORD") ?: ""
        buildConfigField("String", "MAIL_ADDRESS", "\"$mailAddress\"")
        buildConfigField("String", "MAIL_APP_PASSWORD", "\"$mailPassword\"")

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
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")
    implementation("org.jetbrains:annotations:23.0.0")

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    implementation("androidx.room:room-runtime:${roomVersion}")
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)

    annotationProcessor("androidx.room:room-compiler:${roomVersion}")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}