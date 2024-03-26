package com.ndhunju.relay.ui.account

import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.PhoneAlreadyRegisteredException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.AppStateBroadcastService
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.MainDispatcherRule
import com.ndhunju.relay.util.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.time.Duration

/**
 * Tests [AccountViewModel]
 */
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var accountViewModel: AccountViewModel

    private val apiInterfaceMock by lazy {
        mock(ApiInterface::class.java)
    }

    private val appStateBroadcastServiceMock by lazy {
        mock(AppStateBroadcastService::class.java)
    }

    private val analyticsProviderMock by lazy {
        mock(AnalyticsProvider::class.java)
    }

    private val currentUserMock by lazy {
        mock(CurrentUser::class.java)
    }

    private val userMock by lazy {
        User()
    }

    @Before
    fun setUp() {
        accountViewModel = AccountViewModel(
            appStateBroadcastServiceMock,
            analyticsProviderMock,
            apiInterfaceMock,
            currentUserMock,
            userMock
        )
    }

    @After
    fun tearDown() {}

    @Test
    fun `when user enters valid phone numbers then no error message should be shown below phone field`() {
        val validPhoneNumbers = arrayOf(
            "0123456789",
            "012 345 6789",
            "(012) 345-6789",
            "(012)345-6789",
            "+971(012)345-6789",
            "+971 (012) 345-6789",
            "+971 012 345 6789"
        )

        for (validPhoneNumber in validPhoneNumbers) {
            accountViewModel.onPhoneChange(validPhoneNumber)
            Assert.assertNull(accountViewModel.state.value.errorStrIdForPhoneField)
        }
    }

    @Test
    fun `when user enters invalid phone numbers then error message should be shown below phone field`() {
        val inValidPoneNumbers = arrayOf("", "123", "123 COMCAST", "++1 320 7232")

        for (invalidPhoneNumber in inValidPoneNumbers) {
            accountViewModel.onPhoneChange(invalidPhoneNumber)
            Assert.assertEquals(
                R.string.account_invalid_phone,
                accountViewModel.state.value.errorStrIdForPhoneField
            )
        }
    }

    @Test
    fun `when Api returns PhoneAlreadyRegisteredException Then error message should match`() {
        runTest(timeout = Duration.parse("3s")) {
            // Mock the response
            `when`(apiInterfaceMock.postUser())
                .thenReturn(Result.Failure(PhoneAlreadyRegisteredException("")))

            // Call the method in test
            accountViewModel.createNewUserInServer()

            // Assert
            Assert.assertEquals(
                accountViewModel.state.value.errorStrIdForPhoneField,
                R.string.account_phone_registered
            )
        }
    }

    @Test
    fun `when Api returns PhoneAlreadyRegisteredException Then error message should not match generic message`() {
        runBlocking {
            // Mock the response
            `when`(apiInterfaceMock.postUser())
                .thenReturn(Result.Failure(PhoneAlreadyRegisteredException("")))

            // Call the method in test
            accountViewModel.createNewUserInServer()

            // Assert
            Assert.assertNotEquals(
                "When Api returns ${PhoneAlreadyRegisteredException::class.java.simpleName}" +
                        " then error message should not match generic message",
                accountViewModel.state.value.errorStrIdForPhoneField,
                R.string.account_user_create_failed,
            )
        }
    }
}