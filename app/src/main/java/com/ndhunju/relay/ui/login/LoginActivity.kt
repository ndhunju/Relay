package com.ndhunju.relay.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.asLiveData
import com.ndhunju.relay.RelayApplication
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.ui.BaseActivity
import com.ndhunju.relay.ui.MainActivity
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.theme.RelayTheme
import javax.inject.Inject

class LoginActivity: BaseActivity() {

    /**
     * Dagger will provide an instance of [AppStateBroadcastService] from the graph
     */
    @Inject lateinit var appStateBroadcastService: AppStateBroadcastService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFields()

        setContent {
            RelayTheme {
                WelcomeScreen(onClickNext = {
                    supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, AccountFragment.newInstance())
                        .commit()
                })
            }
        }

        // If at any point in time, user is signed in, move to Main Screen
        appStateBroadcastService.isUserSignedIn.asLiveData().observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            }

        }
    }

    /**
     * Injects fields annotated with @Inject
     */
    private fun injectFields() {
        // Make Dagger instantiate @Inject fields in this activity
        (applicationContext as RelayApplication).appComponent.inject(this)
    }

}