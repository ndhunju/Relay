package com.ndhunju.relay.ui.login

import android.content.Intent
import android.os.Bundle
import com.ndhunju.relay.ui.BaseActivity
import com.ndhunju.relay.ui.MainActivity

class LoginActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Show Welcome Fragment
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, WelcomeFragment())
            .commit()

        // If at any point in time, user is signed in, move to Main Screen
        appStateBroadcastService.isUserSignedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            }

        }
    }

}