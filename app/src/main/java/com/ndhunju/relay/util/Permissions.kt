package com.ndhunju.relay.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

/**
 * Checks if all the permissions needed by the app is granted
 */
fun checkIfPermissionGranted(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Request all the permissions needed by the app
 */
fun requestPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
    requestPermissionLauncher.launch(arrayOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    ))
}

/**
 * Returns true if the needed permissions for the app to work are granted in [permissions]
 */
fun areNeededPermissionGranted(permissions: Map<String, Boolean>): Boolean {
    return permissions[Manifest.permission.READ_SMS] == true
            && permissions[Manifest.permission.SEND_SMS] == true
}