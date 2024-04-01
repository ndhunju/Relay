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
    return checkIfSmsPermissionsGranted(context)
            && checkIfPostNotificationPermissionGranted(context)
}

fun checkIfPostNotificationPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // No need to ask for permission before TIRAMISU
    return true
}

fun checkIfSmsPermissionsGranted(context: Context): Boolean {
    return (ActivityCompat.checkSelfPermission(
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
    ) == PackageManager.PERMISSION_GRANTED)
}

/**
 * Request all the permissions needed by the app
 */
fun requestAllPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
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
 * Request all the permissions needed by the app
 */
fun requestSmsPermission(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
    val permissions = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    requestPermissionLauncher.launch(permissions.toTypedArray())
}

fun requestNotificationPermission(requestLauncher: ActivityResultLauncher<Array<String>>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestLauncher.launch(arrayOf( Manifest.permission.POST_NOTIFICATIONS))
    }
}

/**
 * Returns true if the needed permissions for the app to work are granted in [permissions]
 */
fun areNeededPermissionGranted(permissions: Map<String, Boolean>): Boolean {
    return areSmsPermissionGranted(permissions) && isNotificationPermissionGranted(permissions)
}

fun areSmsPermissionGranted(permissions: Map<String, Boolean>): Boolean {
    return (permissions[Manifest.permission.RECEIVE_SMS] == true
            && permissions[Manifest.permission.READ_SMS] == true
            && permissions[Manifest.permission.SEND_SMS] == true)
}

fun isNotificationPermissionGranted(permissions: Map<String, Boolean>):  Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return permissions[Manifest.permission.POST_NOTIFICATIONS] == true
    }

    return true
}