package com.ndhunju.relay.util

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.ndhunju.relay.BuildConfig
import java.util.Locale

val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

class Reference(var value: Int)

@Composable
fun LogCompositions(tag: String, msg: String) {
    if (BuildConfig.DEBUG) {
        val ref = remember { Reference(0) }
        SideEffect { ref.value++ }
        Log.d(tag, "Compositions: $msg ${ref.value}")
    }
}