package com.ndhunju.relay.ui.account

import com.ndhunju.relay.R
import com.ndhunju.relay.api.ApiInterface
import com.ndhunju.relay.api.EmailAlreadyExistException
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.service.AnalyticsManager
import com.ndhunju.relay.service.AppStateBroadcastService
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

    private val analyticsManagerMock by lazy {
        mock(AnalyticsManager::class.java)
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
            analyticsManagerMock,
            apiInterfaceMock,
            currentUserMock,
            userMock
        )
    }

    @After
    fun tearDown() {}

    @Test
    fun `when Api returns EmailAlreadyExistException Then error message should match`() {
        runTest(timeout = Duration.parse("3s")) {
            // Mock the response
            `when`(apiInterfaceMock.postUser())
                .thenReturn(Result.Failure(EmailAlreadyExistException("")))

            // Call the method in test
            accountViewModel.createNewUserInServer()

            // Assert
            Assert.assertEquals(
                accountViewModel.state.value.errorStrIdForEmailField,
                R.string.account_duplicate_email
            )
        }
    }

    @Test
    fun `when Api returns EmailAlreadyExistException Then error message should not match generic message`() {
        runBlocking {
            // Mock the response
            `when`(apiInterfaceMock.postUser())
                .thenReturn(Result.Failure(EmailAlreadyExistException("")))

            // Call the method in test
            accountViewModel.createNewUserInServer()

            // Assert
            Assert.assertNotEquals(
                "When Api returns EmailAlreadyExistException then error message should not match generic message",
                accountViewModel.state.value.errorStrIdForEmailField,
                R.string.account_user_create_failed,
            )
        }
    }
}