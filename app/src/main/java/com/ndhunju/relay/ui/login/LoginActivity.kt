package com.ndhunju.relay.ui.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.theme.RelayTheme

class LoginActivity: FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelayTheme {
                WelcomeScreen(onClickNext = {
                    supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, AccountFragment.newInstance())
                        .commit()
                })
            }
        }
    }
}