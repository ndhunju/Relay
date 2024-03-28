import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")

}

android {
    namespace = "com.ndhunju.relay"
    compileSdk = 34

    val releaseSigningConfig = createReleaseSigningConfig(this)

    defaultConfig {
        applicationId = "com.ndhunju.relay"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (releaseSigningConfig != null) {
                signingConfig = signingConfigs.findByName(releaseSigningConfig)
            }
        }
    }

    // Need to specify at least one flavor dimension.
    flavorDimensions += "version"

    productFlavors {

        create("production") {
            dimension = "version"
            applicationId = "com.ndhunju.relay"
        }

        create("demo") {
            dimension = "version"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            // Set different App Name so that it is easy to differentiate
            resValue("string", "app_name", "Demo Relay")
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
        kotlinCompilerExtensionVersion = "${rootProject.extra["kotlin_compiler_ext_version"]}"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("com.google.android.material:material:1.11.0")

    // Compose
    implementation("androidx.activity:activity-compose:${rootProject.extra["activity_compose_version"]}")
    implementation(platform("androidx.compose:compose-bom:${rootProject.extra["compose_bom_version"]}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    // Declare the dependency for the Cloud Firestore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.code.gson:gson:2.10.1")
    // Analytics library
    implementation("com.google.firebase:firebase-analytics")
    // Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    // Firebase Cloud Messaging (Push Notification)
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    // Dagger
    implementation("com.google.dagger:dagger:2.51")
    ksp("com.google.dagger:dagger-compiler:2.51")

    // Room
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")

    // Encrypted shared preference
    implementation("androidx.security:security-crypto:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:${rootProject.extra["work_version"]}")
    androidTestImplementation("androidx.work:work-testing:${rootProject.extra["work_version"]}") // Test helpers

    // Datastore (Replaces SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Barcode Scanner and Generator
    implementation("com.github.ndhunju:BarcodeScanner:v1.0.1")
    implementation("com.github.ndhunju:BarcodeGenerator:1.0.0")

    // Pull Down To Refresh
    implementation("androidx.compose.material:material:1.6.4")

    // Add the dependencies for the App Check libraries. This library
    // sends App Check tokens along with every request the app makes to Firebase
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    // Add the dependencies for the App Check libraries to be used for debug build type
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:32.7.4"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

}

/**
 * Creates signing config for release and returns the name of the signing config that
 * can be used to assign to a product flavor or build types
 * This is also helpful to print the SHA by running gradle signingReport command
 */
fun createReleaseSigningConfig(baseAppModuleExtension: BaseAppModuleExtension): String? {
    /*
    NOTE: For this to work, create a file inside "keystore" folder. Create folder if needed.
    Inside "keystore" folder, add a file named "release.keystore.properties" with following props
         keyAlias=someKeyAlias
         keyPassword=someKeyPassword
         storeFile=someStoreFileNameAlongWithExtension
         storePassword=someStorePassword
     */
    try {
        val keystoreRootFolderPath = "keystore"
        val keyStorePropsFile = file("$keystoreRootFolderPath/release.keystore.properties")
        val keyStoreProps = Properties().apply { load(keyStorePropsFile.inputStream()) }
        val keyStoreFileName = keyStoreProps["storeFile"] as String

        baseAppModuleExtension.signingConfigs {
            create(keyStoreFileName) {
                keyAlias = keyStoreProps["keyAlias"] as String
                keyPassword = keyStoreProps["keyPassword"] as String
                storeFile = file("$keystoreRootFolderPath/$keyStoreFileName")
                storePassword = keyStoreProps["storePassword"] as String
                println("Done")
            }
        }

        return keyStoreFileName

    } catch (ex: Exception) {
        return null
    }
}