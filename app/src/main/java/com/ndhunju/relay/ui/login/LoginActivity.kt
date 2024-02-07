package com.ndhunju.relay.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.BaseActivity
import com.ndhunju.relay.ui.MainActivity
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.theme.RelayTheme

class LoginActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RelayTheme {
                WelcomeScreen(onClickNext = {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.enter_from_right,
                            R.anim.exit_to_left,
                            R.anim.enter_from_left,
                            R.anim.exit_to_right
                        )
                        .replace(android.R.id.content, AccountFragment.newInstance())
                        .addToBackStack(AccountFragment.TAG)
                        .commit()
                })
            }
        }

        // If at any point in time, user is signed in, move to Main Screen
        appStateBroadcastService.isUserSignedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            }

        }
    }

}