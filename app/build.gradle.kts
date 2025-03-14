plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    val targetVersion = rootProject.ext.get("targetVersion") as Int
    val minVersion = rootProject.ext.get("minVersion") as Int
    val vCode = rootProject.ext.get("vCode") as Int
    val vName = rootProject.ext.get("vName") as String
    val jvmVersion = rootProject.ext.get("jvmVersion") as JavaVersion
    val kotlinJvmVersion = rootProject.ext.get("kotlinJvmVersion") as String

    namespace = "mezzari.torres.lucas.kotlin_network"
    compileSdkVersion(targetVersion)
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "mezzari.torres.lucas.kotlin_network"
        minSdk = minVersion
        targetSdk = targetVersion
        versionCode = vCode
        versionName = vName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }
    kotlinOptions {
        jvmTarget = kotlinJvmVersion
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    //Kotlin
    implementation(libs.kotlin.jdk)

    //Android
    implementation(libs.app.compat)
    implementation(libs.material)

    //Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.expresso)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    //Project
    implementation(project(":network"))
}
