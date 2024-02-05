package com.ndhunju.relay.ui.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.compositeOver
import androidx.fragment.app.FragmentActivity
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.ui.theme.setStatusBarColor

class LoginActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelayTheme {
                WelcomeScreen()
            }
        }
    }
}