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

    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/LICENSE"
            )
        }
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

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gif.drawable)
    implementation(libs.annotations)
    implementation(libs.circle.imageview)
    implementation(libs.recyclerview)
    implementation(libs.coordinatorlayout)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Mail
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // Google Maps
    implementation(libs.play.services.maps)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}