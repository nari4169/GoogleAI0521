plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.billcorea.googleai0521"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.billcorea.googleai0521"
        minSdk = 29
        targetSdk = 35
        versionCode = 13
        versionName = "0.1.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // android.buildTypes.release.ndk.debugSymbolLevel = { SYMBOL_TABLE | FULL }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk.debugSymbolLevel = "full"
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {

    implementation(libs.google.accompanist.permissions)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.generativeai)
    implementation(libs.androidx.foundation.android)
    // This dependency is downloaded from the Google’s Maven repository.
    // So, make sure you also include that repository in your project's build.gradle file.
    implementation(libs.app.update)
    // For Kotlin users also import the Kotlin extensions library for Play In-App Update:
    implementation(libs.app.update.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // https://github.com/raamcosta/compose-destinations 에서 최종 버전을 확인
    implementation(libs.core)
    ksp(libs.ksp)
    implementation (libs.animations.core)
    implementation (libs.wear.core)

    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // coil : 이미지 로딩 라이브러리
    implementation (libs.coil.compose)

    // Glide : 이미지 로딩 라이브러리
    implementation(libs.glide)
    ksp(libs.glide.ksp)

    // ML Kit translate
    implementation (libs.translate)

    // google cloud translate
    implementation (libs.google.cloud.translate)

}