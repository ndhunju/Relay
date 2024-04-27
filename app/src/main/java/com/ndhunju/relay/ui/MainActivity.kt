package com.ndhunju.relay.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.account.AccountAndroidViewModel
import com.ndhunju.relay.ui.account.AccountScreen
import com.ndhunju.relay.ui.account.AccountViewModel
import com.ndhunju.relay.ui.custom.LaunchedEffectOnce
import com.ndhunju.relay.ui.custom.MessageAlertDialog
import com.ndhunju.relay.ui.debug.DebugScreen
import com.ndhunju.relay.ui.debug.DebugViewModel
import com.ndhunju.relay.ui.messagesfrom.MessagesFromView
import com.ndhunju.relay.ui.messagesfrom.MessagesFromViewModel
import com.ndhunju.relay.ui.pair.PairWithParentScreen
import com.ndhunju.relay.ui.pair.PairWithParentViewModel
import com.ndhunju.relay.ui.pair.ShareEncryptionKeyWithQrCodeActivity
import com.ndhunju.relay.ui.parent.ChildUserListFragment
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.areSmsPermissionGranted
import com.ndhunju.relay.util.checkIfSmsPermissionsGranted
import com.ndhunju.relay.util.requestSmsPermission
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    // Member Variables
    private val viewModel: MainViewModel by viewModels { RelayViewModelFactory }
    private lateinit var navController: NavHostController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (areSmsPermissionGranted(permissions)) {
            viewModel.onSmsPermissionGranted()
        } else {
            // Permissions denied
            viewModel.onSmsPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTitle(getString(R.string.app_name))
        showSplashScreenUntilReady()

        setContent {
            navController = rememberNavController()
            RelayTheme {
                NavHost(
                    navController = navController,
                    startDestination = Screen.MessageThread.route
                ) {
                    buildNavGraph()
                }
            }
        }

        bindNavigationCallbacks()

        viewModel.doRequestSmsPermission = {
            requestSmsPermission(requestPermissionLauncher)
        }

        // Check if SMS read and send permissions are granted
        if (checkIfSmsPermissionsGranted(this)) {
            viewModel.onSmsPermissionGranted()
        } else {
            // if (shouldShowRequestPermissionRationale(this, READ_SMS))
            // Always show the rationale
            viewModel.onSmsPermissionDenied()
        }
    }

    /**
     * Builds Navigation Graph
     */
    private fun NavGraphBuilder.buildNavGraph() {

        composable(Screen.MessageThread.route) {
            MainScreen(viewModel = viewModel)
        }

        composable(Screen.PairWithParent.route) {
            // TODO: Nikesh - Is this view model destroyed once this composable is popped?
            val viewModel: PairWithParentViewModel by viewModels { RelayViewModelFactory }
            PairWithParentScreen(
                viewModel.pairedUserPhoneList.collectAsState(),
                viewModel.selectedParentPhoneAddress.collectAsState(),
                viewModel.isSelectedParentPaired.collectAsState(),
                viewModel.showProgress.collectAsState(),
                viewModel.errorMsgResId.collectAsState(),
                onUpPressed = { navController.popBackStack() },
                onClickPairUnPair = viewModel::onClickPairUnpair,
                onClickPairedUser = viewModel::onClickPairedUser,
                onParentPhoneChanged = viewModel::onSelectedParentPhoneChanged
            )
        }

        composable(
            route = Screen.MessagesFrom.pattern,
            //arguments = listOf(navArgument(Screen.MessagesFrom.id) {
            //               defaultValue = 0
            //               type = NavType.StringType
            //          })
        ) { entry ->
            val threadId = entry.arguments?.getString(Screen.MessagesFrom.id) ?: return@composable
            val viewModel: MessagesFromViewModel by viewModels { RelayViewModelFactory }
            viewModel.threadId = threadId
            LaunchedEffectOnce { viewModel.getSmsByThreadId(threadId) }
            MessagesFromView(
                viewModel.senderAddress,
                viewModel.messagesInThread,
                viewModel.isLoading,
                viewModel.textMessage,
                viewModel.onTextMessageChange,
                viewModel::sendMessage,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(Screen.Account.route) {
            val accountViewModel: AccountAndroidViewModel by viewModels { RelayViewModelFactory }
            val uiState = accountViewModel.instance.state.collectAsStateWithLifecycle()
            AccountScreen(
                accountScreenUiState = uiState.value,
                showUpButton = true,
                onNameChange = accountViewModel.instance.onNameChange,
                onPhoneChange = accountViewModel.instance.onPhoneChange,
                onEncKeyChange = accountViewModel.instance.onEncKeyChange,
                onClickCreateUpdate = accountViewModel.instance.onClickCreateUpdateUser,
                onUpPressed = { navController.popBackStack() },
                onClickDialogBtnOk = accountViewModel.instance.onClickDialogBtnOk,
                onclickDeleteAccount = {
                    showDialog(getString(R.string.account_message_deletion_feature))
                    accountViewModel.instance.onClickDeleteAccount
                }
            )
        }

        composable(Screen.Debug.route) {
            val debugViewModel: DebugViewModel by viewModels { RelayViewModelFactory }
            DebugScreen(
                onClickForceCrash = debugViewModel.onClickForceCrashItem,
                onUpPressed = { navController.popBackStack() }
            )
        }
    }

    private fun bindNavigationCallbacks() {

        viewModel.doOpenPairWithParentScreen = {
            navController.navigate(Screen.PairWithParent.route)
        }

        viewModel.doOpenEncryptionKeyScreen = {
            startActivity(Intent(this, ShareEncryptionKeyWithQrCodeActivity::class.java))
        }

        viewModel.doOpenMessageFromFragment = { message ->
            navController.navigate(Screen.MessagesFrom(message.threadId).route)
        }

        viewModel.doOpenAccountScreen = {
            navController.navigate(Screen.Account.route)
        }

        viewModel.doOpenChildUserFragment = {
            // TODO: Nikesh - Use compose navigation here as well
            ChildUserListFragment.addToContent(supportFragmentManager)
        }

        viewModel.doOpenDebugScreen = {
            navController.navigate(Screen.Debug.route)
        }

    }

    /**
     * Shows splash screen until the view model has finished loading data
     */
    private fun showSplashScreenUntilReady() {
        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check whether the initial data is ready.
                    return if (viewModel.showSplashScreen.value) {
                        // The data isn't ready.
                        // Suspend first draw which would hide splash screen
                        false
                    } else {
                        // The content is ready. Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    }
                }
            }
        )
    }

}