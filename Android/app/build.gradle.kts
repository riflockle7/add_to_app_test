@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.util.Properties

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

val properties = Properties()

val versionPropertiesFile = File("../version.properties")
if (versionPropertiesFile.exists()) {
    properties.load(FileInputStream(versionPropertiesFile))
}

val addToAppVersionCode: String? = properties.getProperty("versionCode")
val addToAppVersionName: String? = properties.getProperty("versionName")
if (addToAppVersionName == null || addToAppVersionCode == null) {
    throw GradleException("versionName not found. Define versionName in the version.properties file.")
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.1"
    namespace = "com.example.addtoappexample"

    defaultConfig {
        applicationId = "com.example.addtoappexample"
        minSdk = 24
        targetSdk = 33
        versionCode = Integer.parseInt(addToAppVersionCode)
        versionName = addToAppVersionName

        testApplicationId = "com.example.addtoappexample.test"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"

        resourceConfigurations += listOf("en", "ko")
    }

    useLibrary("org.apache.http.legacy")

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["firebasePerformanceLogcatEnabled"] = true
            extra.set("enableCrashlytics", false)
            extra.set("alwaysUpdateBuildId", false)
            isCrunchPngs = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            ndk {
                abiFilters.add("arm64-v8a")
                abiFilters.add("x86_64") // 32bit 기기 이용시 "armeabi-v7a" 추가
            }
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false
            manifestPlaceholders["firebasePerformanceLogcatEnabled"] = false
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-retrofit_okhttp3.pro",
                "proguard-gson.pro",
                "proguard-glide.pro",
                "proguard-simple-xml.pro",
                "proguard-sticker.pro",
            )
            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    flavorDimensions += "default"

    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
        }
        create("real") {
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            resources.srcDirs("src/main/resources")
            res.srcDirs("src/main/res")
        }
        getByName("dev") {
            resources.srcDirs("src/dev/res")
        }
    }

    lint {
        abortOnError = false
        disable += listOf(
            "MissingTranslation", "RtlHardcoded", "ContentDescription", "SpUsage", "ResourceType", "InvalidPackage", "KeyboardInaccessibleWidget", "LogNotTimber", "StringFormatInTimber", "ThrowableNotAtBeginning", "BinaryOperationInTimber", "TimberArgCount", "TimberArgTypes", "TimberTagLength"
        )
        checkGeneratedSources = false
    }

    packagingOptions.resources.excludes += listOf(
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE.txt",
        "META-INF/LICENSE",
        "META-INF/NOTICE.txt",
        "META-INF/NOTICE",
        "META-INF/rxjava.properties",
        "META-INF/*.kotlin_module",
        "META-INF/gradle/incremental.annotation.processors",
    )

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    configurations.all {
        exclude("xpp3")
    }
}

dependencies {
    implementation("junit:junit:4.13.2")
    implementation(project(":flutter"))
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

configurations.all {
    // check for updates every build
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}