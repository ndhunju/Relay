package com.ndhunju.relay.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

/**
 * Checks if all the permissions needed by the app is granted
 */
fun checkIfPermissionGranted(context: Context): Boolean {
    var areGranted =  ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       areGranted = areGranted && ActivityCompat.checkSelfPermission(
           context,
           Manifest.permission.POST_NOTIFICATIONS
       ) == PackageManager.PERMISSION_GRANTED
    }

    return areGranted

}

/**
 * Request all the permissions needed by the app
 */
fun requestPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
    val permissions = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    requestPermissionLauncher.launch(permissions.toTypedArray())
}

/**
 * Returns true if the needed permissions for the app to work are granted in [permissions]
 */
fun areNeededPermissionGranted(permissions: Map<String, Boolean>): Boolean {
    var areGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
            && permissions[Manifest.permission.READ_SMS] == true
            && permissions[Manifest.permission.SEND_SMS] == true

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        areGranted = areGranted && permissions[Manifest.permission.POST_NOTIFICATIONS] == true
    }

    return areGranted
}