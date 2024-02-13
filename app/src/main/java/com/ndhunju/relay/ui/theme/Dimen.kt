package com.ndhunju.relay.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// See https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes
data class Dimensions(
    val contentPaddingHorizontal: Dp = 16.dp,
    val itemPaddingVertical: Dp,
    val dividerHeight: Dp = 1.dp,

    // Welcome Screen
    val welcomeLauncherIconSize: Dp = 128.dp,
    val welcomeHeaderTextSize: TextUnit = 32.sp,
    val welcomeBodyTextSize: TextUnit = 18.sp
)

val CompactDimensions = Dimensions(
    itemPaddingVertical = 16.dp
)

val Sw600Dimensions = Dimensions(
    contentPaddingHorizontal = 24.dp,
    itemPaddingVertical = 24.dp,
)

val Sw840Dimensions = Dimensions(
    contentPaddingHorizontal = 32.dp,
    itemPaddingVertical = 32.dp,
    dividerHeight = 2.dp,

    // Welcome Screen
    welcomeLauncherIconSize = 128.dp,
    welcomeHeaderTextSize = 64.sp,
    welcomeBodyTextSize = 36.sp
)


val LocalDimens = staticCompositionLocalOf { CompactDimensions }