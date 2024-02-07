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
        appStateBroadcastService.isUserSignedIn.observe(this) { isSignedIn ->
            if (isSignedIn) {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    // When user presses back, don't bring them back to this activity
                    flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                finish()
            }

        }
    }

}