package com.ndhunju.barcode

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Returns a color int value of current string that represents color in hex format,
 * after applying passed [percentage] to the color int.
 */
@ColorInt
fun String.toColorIntWithOpacity(@IntRange(from = 0, to = 100) percentage: Int): Int {

    // Validate the format of the string
    if (length != 7 && length != 9) return 0

    // Add or override alpha value in the color with the passed opacity
    var alphaInHexStr = Integer.toHexString(255.times(percentage).div(100))
    alphaInHexStr = if (alphaInHexStr.length == 1) "0$alphaInHexStr" else alphaInHexStr
    val colorStrExcludingHash = this.subSequence(if (length == 7) 1 else 3, 7)

    // Return color string after setting the alpha value
    return Color.parseColor("#$alphaInHexStr$colorStrExcludingHash")
}

/**
 * True if the passed [permission] was granted.
 */
fun Context.isPermissionGranted(permission: String) : Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    )== PackageManager.PERMISSION_GRANTED
}

/**
 * Starts requesting passed [permissions] to the user.
 */
fun Context.requestPermission(requestCode: Int, vararg permissions: String) {

    if (this is Activity) {
        ActivityCompat.requestPermissions(
            this,
            permissions,
            requestCode
        )
    } else {
        throw Exception("The context should be of type Activity")
    }
}

fun Context.isInPortraitMode(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}