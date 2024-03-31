package com.ndhunju.relay.ui.account

import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterfaceDummyImpl
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


class AccountViewModelTest {

    @get:Rule val composeRule = createComposeRule()
    // Use below if you need an activity to test
    //@get:Rule val activityComposeRule = createAndroidComposeRule<MainActivity>()

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    private lateinit var appStateBroadcastService: AppStateBroadcastServiceImpl
    private lateinit var analyticsProvider: AnalyticsProvider
    private lateinit var apiInterface: ApiInterfaceDummyImpl
    private lateinit var currentUser: CurrentUser

    @Before
    fun setUp() {
        currentUser = InMemoryCurrentUser()
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
            appStateBroadcastService,
            analyticsProvider,
            apiInterface,
            currentUser,
            newUser
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
            appStateBroadcastService,
            analyticsProvider,
            apiInterface,
            currentUser,
            newUser
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
            appStateBroadcastService,
            analyticsProvider,
            apiInterface,
            currentUser,
            newUser
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

}