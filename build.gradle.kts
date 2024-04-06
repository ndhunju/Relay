// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    dependencies {
        //classpath("com.google.gms:google-services:4.4.0")
        // Add the Maven coordinates and latest version of the plugin
        classpath ("com.google.gms:google-services:4.4.1")
    }

    extra.apply {
        set("room_version", "2.6.1")
        set("work_version", "2.9.0")
        set("compose_bom_version", "2023.08.00")
        set("activity_compose_version", "1.8.2")
        set("kotlin_compiler_ext_version", "1.5.1")
    }
}

plugins {
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.android.library") version "8.3.1" apply false
    id("com.android.test") version "8.3.1" apply false
    id("androidx.baselineprofile") version "1.2.3" apply false
    id("androidx.benchmark") version "1.2.3" apply false

}