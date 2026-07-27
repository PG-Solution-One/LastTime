@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

val initialVersionName = "1.0.0"
val releaseTag =
    providers
        .gradleProperty("releaseTag")
        .orNull
        ?.trim()
        ?.takeIf(String::isNotEmpty)
val exactGitTag =
    runCatching {
        providers
            .exec {
                commandLine("git", "describe", "--tags", "--exact-match", "HEAD")
                isIgnoreExitValue = true
            }.standardOutput
            .asText
            .get()
            .trim()
            .takeIf(String::isNotEmpty)
    }.getOrNull()
val latestGitTag =
    exactGitTag
        ?: runCatching {
            providers
                .exec {
                    commandLine("git", "describe", "--tags", "--abbrev=0", "HEAD")
                    isIgnoreExitValue = true
                }.standardOutput
                .asText
                .get()
                .trim()
                .takeIf(String::isNotEmpty)
        }.getOrNull()
val appVersionName = releaseTag ?: exactGitTag ?: "${latestGitTag ?: initialVersionName}-dev"
val apkVersionName = appVersionName.replace(Regex("[^0-9A-Za-z._-]"), "-")

android {
    namespace = "app.lasttime"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "app.lasttime"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("LastTime-$apkVersionName.apk")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.work.testing)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
