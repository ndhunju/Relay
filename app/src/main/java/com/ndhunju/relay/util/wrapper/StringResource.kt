package com.ndhunju.relay.util.wrapper

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Simple wrapper class to hold references to string resource id and placeholder args
 */
class StringResource(@StringRes val id: Int, vararg val formatArgs: Any)

fun Context.getString(stringResource: StringResource?): String? {
    if (stringResource == null) return null
    return getString(stringResource.id, *stringResource.formatArgs)
}

@Composable
fun stringResource(stringResource: StringResource?): String? {

    if (stringResource == null) {
        return null
    }

    return stringResource(
        id = stringResource.id,
        /** Use the spread operator in front of vararg
         *  here to pass each items individually **/
        *stringResource.formatArgs
    )
}