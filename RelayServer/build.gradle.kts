plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Fixme: Is throwing following error
    // Duplicate class com.google.type.TimeZoneOrBuilder found in modules proto-google-common-
    // protos-2.20.0 (com.google.api.grpc:proto-google-common-protos:2.20.0) and protolite-well-
    // known-types-18.0.0-runtime (com.google.firebase:protolite-well-known-types:18.0.0)
    implementation("com.google.firebase:firebase-admin:9.2.0")
}