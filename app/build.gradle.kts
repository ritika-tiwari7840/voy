import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

val secretPropertiesFile = rootProject.file("secret.properties")
val secretProperties = Properties()
secretProperties.load(secretPropertiesFile.inputStream())

android {
    namespace = "com.ritika.voy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ritika.voy"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "MAP_API_KEY",
            "\"${secretProperties["API_KEY"]}\"")


        manifestPlaceholders["API_KEY"] = secretProperties["API_KEY"] as String
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
    buildFeatures {
        viewBinding =true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.cronet.embedded)

    //navigation dependencies
    val nav_version = "2.8.3"

    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0") // Check for the latest version


    //retrofit and datastore

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")


    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.material.v190)




    //Retrofit Dependencies
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0-alpha07")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //map dependencies
    implementation ("com.google.android.gms:play-services-maps:19.0.0")

    //places sdk
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("com.google.android.libraries.places:places:3.5.0")

    //websockets
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}