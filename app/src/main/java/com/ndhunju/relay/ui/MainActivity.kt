package com.ndhunju.relay.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.account.AccountAndroidViewModel
import com.ndhunju.relay.ui.account.AccountScreen
import com.ndhunju.relay.ui.custom.LaunchedEffectOnce
import com.ndhunju.relay.ui.debug.DebugScreen
import com.ndhunju.relay.ui.debug.DebugViewModel
import com.ndhunju.relay.ui.messagesfrom.MessagesFromScreen
import com.ndhunju.relay.ui.messagesfrom.MessagesFromViewModel
import com.ndhunju.relay.ui.pair.AddChildEncryptionKeyFromQrCodeActivity
import com.ndhunju.relay.ui.pair.PairWithParentScreen
import com.ndhunju.relay.ui.pair.PairWithParentViewModel
import com.ndhunju.relay.ui.pair.ShareEncryptionKeyWithQrCodeActivity
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.ui.parent.ChildUserListScreen
import com.ndhunju.relay.ui.parent.ChildUserListViewModel
import com.ndhunju.relay.ui.parent.messagesfromchild.MessagesFromChildScreen
import com.ndhunju.relay.ui.parent.messagesfromchild.MessagesFromChildViewModel
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.areSmsPermissionGranted
import com.ndhunju.relay.util.checkIfSmsPermissionsGranted
import com.ndhunju.relay.util.isNotificationPermissionGranted
import com.ndhunju.relay.util.requestNotificationPermission
import com.ndhunju.relay.util.requestSmsPermission

class MainActivity : BaseActivity() {

    // Member Variables
    private val viewModel: MainViewModel by viewModels { RelayViewModelFactory }
    private lateinit var navController: NavHostController

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
            MainScreen(viewModel)
        }

        composable(Screen.PairWithParent.route) {
            PairWithParentScreenWithViewModel()
        }

        composable(Screen.MessagesFrom.routeWithPlaceHolders) { entry ->
            MessageFromScreenWithViewModel(entry)
        }

        composable(Screen.Account.route) {
            AccountScreenWithViewModel()
        }

        composable(Screen.Debug.route) {
            DebugScreenWithViewModel()
        }

        composable(Screen.ChildUserList.route) {
            ChildUserListScreenWithViewModel()
        }

        composable(
            route = Screen.MessagesFromChild.routeWithPlaceHolders,
            arguments = listOf(
                navArgument(Screen.MessagesFromChild.userIdKey) { type = NavType.StringType },
                navArgument(Screen.MessagesFromChild.phoneKey) { defaultValue = "" }
            )
        ) { navBackStackEntry ->
            MessagesFromChildWithViewModel(navBackStackEntry)
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

        viewModel.doOpenChildUserListScreen = {
            navController.navigate(Screen.ChildUserList.route)
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

    //region Composable Screen bound with respective View Models

    @Composable
    private fun DebugScreenWithViewModel() {
        val debugViewModel: DebugViewModel by viewModels { RelayViewModelFactory }
        DebugScreen(debugViewModel) { navController.popBackStack() }
    }

    @Composable
    private fun PairWithParentScreenWithViewModel() {
        // TODO: Nikesh - Is this view model destroyed once this composable is popped?
        val viewModel: PairWithParentViewModel by viewModels { RelayViewModelFactory }
        PairWithParentScreen(viewModel) { navController.popBackStack() }
    }

    @Composable
    private fun AccountScreenWithViewModel() {
        val accountViewModel: AccountAndroidViewModel by viewModels { RelayViewModelFactory }
        AccountScreen(
            accountViewModel,
            onclickDeleteAccount = {
                showDialog(getString(R.string.account_message_deletion_feature))
                accountViewModel.instance.onClickDeleteAccount
            }
        ) { navController.popBackStack() }
    }

    @Composable
    private fun MessageFromScreenWithViewModel(entry: NavBackStackEntry) {
        val viewModel: MessagesFromViewModel by viewModels { RelayViewModelFactory }
        viewModel.threadId = entry.arguments?.getString(Screen.MessagesFrom.threadIdKey)
            ?: return
        MessagesFromScreen(viewModel) { navController.popBackStack() }
    }

    @Composable
    private fun MessagesFromChildWithViewModel(navBackStackEntry: NavBackStackEntry) {
        val viewModel: MessagesFromChildViewModel by viewModels { RelayViewModelFactory }
        viewModel.childUserId = navBackStackEntry.arguments?.getString(
            Screen.MessagesFromChild.userIdKey
        ) ?: return
        viewModel.childUserPhone = navBackStackEntry.arguments?.getString(
            Screen.MessagesFromChild.phoneKey
        ) ?: return

        MessagesFromChildScreen(viewModel) { navController.popBackStack() }
    }

    @Composable
    fun ChildUserListScreenWithViewModel() {

        val viewModel: ChildUserListViewModel by viewModels { RelayViewModelFactory }

        onResultNotificationPermission = { permissions ->
            if (isNotificationPermissionGranted(permissions).not()) {
                viewModel.onDeniedNotificationPermission()
            }
        }

        onResultFromAddChildEncryptionKeyFromQrCodeActivity = { resultCode ->
            if (resultCode == RESULT_OK) {
                viewModel.invalidateChildUsers()
            }
        }

        viewModel.doOpenMessagesFromChildFragment = { child ->
            navController.navigate(Screen.MessagesFromChild(child.id, child.phone).route)
        }

        viewModel.doOpenAddChildEncryptionKeyFromQrCodeFragment = { _: Child ->
            addChildEncryptionKeyFromQrCodeActivityLauncher.launch(
                Intent(this@MainActivity, AddChildEncryptionKeyFromQrCodeActivity::class.java)
            )
        }

        viewModel.doRequestNotificationPermission = {
            requestNotificationPermission(requestNotificationPermissionLauncher)
        }

        LaunchedEffectOnce { viewModel.onViewCreated() }
        ChildUserListScreen(
            viewModel = viewModel,
            onUpPressed = { navController.popBackStack() }
        )
    }

    //endregion

    //region Activity Launchers and onResult Receivers
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

    private var onResultFromAddChildEncryptionKeyFromQrCodeActivity: ((Int) -> Unit)? = null

    private val addChildEncryptionKeyFromQrCodeActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onResultFromAddChildEncryptionKeyFromQrCodeActivity?.invoke(result.resultCode)
    }

    private var onResultNotificationPermission: ((Map<String, Boolean>) -> Unit)? = null

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        onResultNotificationPermission?.invoke(permissions)
    }

    //endregion

}