package com.ndhunju.relay.ui.account

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterfaceDummyImpl
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.api.response.Settings
import com.ndhunju.relay.service.AppStateBroadcastServiceImpl
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.service.analyticsprovider.LocalAnalyticsProvider
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.InMemoryCurrentUser
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.connectivity.NougatNetworkConnectionChecker
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AccountScreenTest {

    @get:Rule val composeRule = createComposeRule()
    // Use below if you need an activity to test
    //@get:Rule val activityComposeRule = createAndroidComposeRule<MainActivity>()

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    /* Note: Here were are using Fakes as opposed to Mocks in AccountViewModelTest */
    private lateinit var appStateBroadcastService: AppStateBroadcastServiceImpl
    private lateinit var analyticsProvider: AnalyticsProvider
    private lateinit var apiInterface: ApiInterfaceDummyImpl
    private lateinit var currentUser: CurrentUser

    @Before
    fun setUp() {
        currentUser = InMemoryCurrentUser()
        apiInterface = ApiInterfaceDummyImpl()
        analyticsProvider = LocalAnalyticsProvider()
        appStateBroadcastService = AppStateBroadcastServiceImpl(
            NougatNetworkConnectionChecker(context),
            currentUser
        )
    }

    @After
    fun tearDown() {}

    @Test
    fun givenAUserWhenAccountScreenIsOpenThenPhoneNumberFieldShouldEnabled() {
        // Given the user is registered
        val newUser = User(isRegistered = false)
        currentUser.user = newUser

        val accountViewModel = AccountViewModel(
            appStateBroadcastService = appStateBroadcastService,
            analyticsProvider= analyticsProvider,
            apiInterface = apiInterface,
            currentUser = currentUser,
            user = newUser
        )

        // When Account Screen is opened
        composeRule.setContent {
            AccountScreen(accountViewModel.state.collectAsStateWithLifecycle().value)
        }

        // Then
        composeRule.onNode(
            hasTestTag(context.getString(R.string.text_field_label_phone))
        ).assert(isEnabled()) {
            "When the user is not registered, phone number filed should be enabled"
        }
    }

    @Test
    fun givenARegisteredUserWhenAccountScreenIsOpenThenPhoneNumberFieldShouldBeDisabled() {
        // Given the user is registered
        val newUser = User(isRegistered = true)
        currentUser.user = newUser

        val accountViewModel = AccountViewModel(
            appStateBroadcastService = appStateBroadcastService,
            analyticsProvider= analyticsProvider,
            apiInterface = apiInterface,
            currentUser = currentUser,
            user = newUser
        )

        // When Account Screen is opened
        composeRule.setContent {
            AccountScreen(accountViewModel.state.collectAsStateWithLifecycle().value)
        }

        // Then
        composeRule.onNode(
            hasTestTag(context.getString(R.string.text_field_label_phone))
        ).assert(isNotEnabled()) {
            "When the user is registered phone number filed should be disabled"
        }
    }

    @Test
    fun givenARegisteredUserWhenAccountScreenIsOpenThenPhoneNumberFieldIsFilled() {
        // Given
        val testPhoneNumber = "123456789"
        val newUser = User(isRegistered = true, phone = testPhoneNumber)
        currentUser.user = newUser

        val accountViewModel = AccountViewModel(
            appStateBroadcastService = appStateBroadcastService,
            analyticsProvider= analyticsProvider,
            apiInterface = apiInterface,
            currentUser = currentUser,
            user = newUser
        )

        // When
        composeRule.setContent {
            AccountScreen(accountViewModel.state.collectAsStateWithLifecycle().value)
        }

        // Then
        composeRule.onNode(
            hasTestTag(context.getString(R.string.text_field_label_phone))
        ).assert(hasText(testPhoneNumber)) {
            "When the user is registered, the phone number should show on the phone text field"
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun givenAllFieldsAreEnteredWhenCreateBtnIsClickedThenUserAccountShouldBeCreated() {
        // Initial Setup
        val testPhoneNumber = "123456789"
        val testEncKey = "encryptionKey"
        apiInterface = object: ApiInterfaceDummyImpl() {

            override suspend fun getSettings(): Result<Settings> {
                return Result.Success(Settings(byPassAccountCreationNumber = testPhoneNumber))
            }

            override suspend fun postUser(name: String?, phone: String?): Result<String> {
                return Result.Success("userId123")
            }
        }

        val accountViewModel = AccountViewModel(
            appStateBroadcastService = appStateBroadcastService,
            analyticsProvider= analyticsProvider,
            apiInterface = apiInterface,
            currentUser = currentUser,
            user = currentUser.user
        )

        composeRule.setContent {
            AccountScreen(
                accountScreenUiState = accountViewModel.state.collectAsStateWithLifecycle().value,
                onNameChange = accountViewModel.onNameChange,
                onPhoneChange = accountViewModel.onPhoneChange,
                onEncKeyChange = accountViewModel.onEncKeyChange,
                onClickCreateUpdate = accountViewModel.onClickCreateUpdateUser,
                onClickDialogBtnOk = accountViewModel.onClickDialogBtnOk
            )
        }

        // Given the phone and encryption key are provided
        val phoneTextField = composeRule
            .onNode(hasTestTag(context.getString(R.string.text_field_label_phone)))
        phoneTextField.performTextReplacement(testPhoneNumber)

        val encKeyTextField = composeRule
            .onNode(hasTestTag(context.getString(R.string.text_field_label_enc_key)))
        encKeyTextField.performTextReplacement(testEncKey)

        //composeTestRule.mainClock.autoAdvance = false // default
        //composeTestRule.waitForIdle() //  // Advances the clock until Compose is idle
        // To wait for Idling Resources to become idle, set autoAdvance to false

        composeRule.waitUntilExactlyOneExists(hasText(testPhoneNumber))
        composeRule.waitUntilExactlyOneExists(hasText(testEncKey))

        // When "Create" button is clicked
        composeRule
            .onNode(hasAnyChild(hasText(context.getString(R.string.button_label_create_account))), true)
            .performClick() //.printToLog("888")

        // Then assert that the user is signed in
        composeRule.waitUntil(2_000) {
            appStateBroadcastService.isUserSignedIn.value == true
        }
        assert(appStateBroadcastService.isUserSignedIn.value == true)

    }

}