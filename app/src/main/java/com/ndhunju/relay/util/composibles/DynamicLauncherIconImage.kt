package com.ndhunju.relay.util.composibles

import android.content.Context
import android.graphics.drawable.LayerDrawable
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.ndhunju.relay.R

/**
 * Dynamically creates launcher icon based on dynamic theme set by the user.
 * The end icon is same as [R.drawable.ic_launcher_foreground] but the colors
 * are picked from [MaterialTheme.colorScheme]
 */
@Composable
fun DynamicLauncherIconImage(
    modifier: Modifier
) {
    Image(
        bitmap = dynamicLauncherIconBitmap(),
        // Specify that this image has no semantic meaning
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun dynamicLauncherIconBitmap(): ImageBitmap  {

    return dynamicLauncherIconBitmap(
        LocalContext.current,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )

}

@Composable
fun dynamicLauncherIconBitmap(
    context: Context,
    primary: Color,
    secondary: Color,
    tertiary: Color
): ImageBitmap {

    /*
     * `remember` stores the value until it leaves the Composition. However, there is a way to
     * invalidate the cached value. The remember API also takes a key or keys parameter. If any
     *  of these keys change, the next time the function recomposes, remember invalidates the
     * cache and executes the calculation lambda block again. This mechanism gives you control
     *  over the lifetime of an object in the Composition. The calculation remains valid until
     * the inputs change, instead of until the remembered value leaves the Composition.
     */

    val dynamicLauncherBitmap = remember(key1 = primary, key2 = secondary, key3 = tertiary) {

        val outer = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground_outer)
        outer?.setTint(primary.toArgb())
        val innerTop = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground_inner_top)
        innerTop?.setTint(secondary.toArgb())
        val innerBottom = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground_inner_bottom)
        innerBottom?.setTint(tertiary.toArgb())

        //Log.d("TAG", "dynamicLauncherIconBitmap: called")

        LayerDrawable(arrayOf(
                outer,
                innerTop,
                innerBottom
        )).toBitmap().asImageBitmap()
    }

    return dynamicLauncherBitmap
}