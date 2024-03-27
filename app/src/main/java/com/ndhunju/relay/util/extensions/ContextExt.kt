package com.ndhunju.relay.util.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.ndhunju.relay.R

val userColorRefs = arrayOf(
    R.color.user_color_red,
    R.color.user_color_blue,
    R.color.user_color_green,
    R.color.user_color_pink,
    R.color.user_color_yellow,
    R.color.user_color_sky_blue,

    )
fun Context.getColorForId(uniqueId: Int): Color {
    return Color(getColor(userColorRefs[uniqueId.mod(userColorRefs.size)]))
}

/**
 * Gets version code of current app
 */
fun Context.getAppVersionCode(): Long {

    val info = packageManager.getPackageInfo(applicationContext.packageName, 0)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        info.versionCode.toLong()
    }

}

/**
 * Opens [url] if it is correctly formatted URL
 */
fun Context.openIfLink(url: String?) {

    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    } catch (_: Exception) {}

}