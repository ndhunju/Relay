// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        //classpath("com.google.gms:google-services:4.4.0")
        // Add the Maven coordinates and latest version of the plugin
        classpath ("com.google.gms:google-services:4.4.0")
    }

    extra.apply {
        set("room_version", "2.6.1")
        set("work_version", "2.9.0")
    }
}

plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
}