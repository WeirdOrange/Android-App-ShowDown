import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists() && localPropertiesFile.canRead()) {
    localProps.load(localPropertiesFile.inputStream())
    println("Local Property file found")
} else {
    println("Error finding")
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
//        val localProperties = Properties()
//        val localPropertiesFile = rootProject.file("local.properties")
//        if (localPropertiesFile.exists()) {
//            localProperties.load(FileInputStream(localPropertiesFile))
//        }
        val mailAddress = localProps.getProperty("EMAIL_ADDRESS") ?: ""
        val mailPassword = localProps.getProperty("EMAIL_APP_PASSWORD") ?: ""
        buildConfigField("String", "EMAIL_ADDRESS", "\"${mailAddress}\"")
        buildConfigField("String", "EMAIL_APP_PASSWORD", "\"${mailPassword}\"")
//        buildConfigField("String", "MAIL_ADDRESS", "\"${localProps["MAIL_ADDRESS"]}\"")
//        buildConfigField("String", "MAIL_APP_PASSWORD", "\"${localProps["MAIL_APP_PASSWORD"]}\"")
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

        buildFeatures {
            buildConfig = true 
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

println("Loaded MAIL_ADDRESS = ${localProps.getProperty("sdk.dir")}")
println("Loaded MAIL_ADDRESS = ${localProps.getProperty("TEST")}")
println("Loaded MAIL_ADDRESS = ${localProps.getProperty("EMAIL_ADDRESS")}")
println("Loaded MAIL_APP_PASSWORD = ${localProps.getProperty("EMAIL_APP_PASSWORD")}")