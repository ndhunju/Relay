package com.ndhunju.relay.util.extensions

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView

fun TextView.setTextAndVisibility(text: String?) {
    if (text.isNullOrEmpty()) {
        this.text = null
        this.visibility = GONE
    } else {
        this.text = text
        this.visibility = VISIBLE
    }
}