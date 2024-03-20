package com.ndhunju.relay.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val GreenYellow80 = Color(0xffccff33)
val Green60 = Color(0xff57cc99)
val RedOrange70 = Color(0xffe26d5c)

data class Colors(
    val success: Color = Green60,
    val failure: Color = RedOrange70
)

val ColorsForDarkTheme = Colors(
    success = GreenYellow80
)
val ColorsForLightTheme = Colors(
    success = Green60
)

val LocalColors = staticCompositionLocalOf { ColorsForDarkTheme }
