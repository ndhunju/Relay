package com.ndhunju.relay.util.extensions

import android.content.Context
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