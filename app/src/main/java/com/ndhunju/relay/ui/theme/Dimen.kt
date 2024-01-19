package com.ndhunju.relay.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// See https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes
data class Dimensions(
    val contentPaddingHorizontal: Dp = 16.dp,
    val itemPaddingVertical: Dp,
    val dividerHeight: Dp = 1.dp
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
    dividerHeight = 2.dp
)


val LocalDimens = staticCompositionLocalOf { CompactDimensions }