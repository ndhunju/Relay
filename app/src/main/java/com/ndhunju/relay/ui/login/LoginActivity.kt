package com.ndhunju.relay.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.BaseActivity
import com.ndhunju.relay.ui.MainActivity
import com.ndhunju.relay.ui.Screen
import com.ndhunju.relay.ui.account.AccountAndroidViewModel
import com.ndhunju.relay.ui.account.AccountScreen
import com.ndhunju.relay.ui.account.AccountViewModel
import com.ndhunju.relay.ui.custom.MessageAlertDialog
import com.ndhunju.relay.ui.theme.RelayTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Show Welcome Screen
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.Welcome.route) {
                composable(Screen.Welcome.route) {
                    // TODO: Nikesh - Add custom animation to enter from sideways
                    WelcomeScreen(onClickNext = { navController.navigate(Screen.Account.route) })
                }
                composable(Screen.Account.route) {
                    val accountViewModel: AccountAndroidViewModel by viewModels { RelayViewModelFactory }
                    showAccountUnverifiedDialogIfNotified(accountViewModel.instance)
                    AccountScreen(accountViewModel) { navController.popBackStack() }
                }
            }
        }

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

    private fun showAccountUnverifiedDialogIfNotified(accountViewModel: AccountViewModel) {
        lifecycleScope.launch {
            var dialog: View? = null
            accountViewModel.showAccountUnverifiedDialog.collectLatest { show ->
                if (show) {
                    dialog = showDialog {
                        RelayTheme {
                            MessageAlertDialog(
                                stringResource(R.string.account_verification_dialog_message),
                                accountViewModel.onClickAccountUnverifiedDialogBtn,
                                accountViewModel.onClickAccountUnverifiedDialogBtn
                            )
                        }
                    }
                } else {
                    if (dialog != null) {
                        (findViewById<ViewGroup>(android.R.id.content)).removeView(dialog)
                    }
                }
            }
        }
    }

}