package com.ndhunju.relay.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.ndhunju.relay.R
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.custom.SyncStatusIcon
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.theme.Colors
import com.ndhunju.relay.ui.theme.LocalColors
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


class MainScreenUiTest {

    @get:Rule val composeTestRule = createComposeRule()
    private val context by lazy {
        // Use targetContext to get the resources from the main folder.
        // Use context to get the resources from the test folder.
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun whenThenSyncStatusIsSuccessThenTheSyncStatusIconShouldHaveGreenTint() {
        var colorsForCurrentTheme = Colors()
        val message = Message(
            "id",
            "threadId",
            "from",
            "body",
            0L,
            "type",
            Result.Success()
        )

        composeTestRule.setContent {
            // Initialize MainContent with fake messages
            colorsForCurrentTheme = LocalColors.current
            MainContent(
                lastMessageList =  remember { mutableStateListOf(message) },
                showErrorMessageForPermissionDenied = MutableStateFlow(false).collectAsState()
            )
        }

        assertSyncStatusIconMatchesForGivenMessage(message, colorsForCurrentTheme)
    }

    @Test
    fun whenTheSyncStatusIsFailureThenTheSyncStatusIconShouldHaveRedTint() {
        var colorsForCurrentTheme = Colors()
        val message = Message(
            "id",
            "threadId",
            "from",
            "body",
            0L,
            "type",
            Result.Failure()
        )

        composeTestRule.setContent {
            // Initialize MainContent with fake messages
            colorsForCurrentTheme = LocalColors.current
            MainContent(
                lastMessageList =  remember { mutableStateListOf(message) },
                showErrorMessageForPermissionDenied = MutableStateFlow(false).collectAsState()
            )
        }

        assertSyncStatusIconMatchesForGivenMessage(message, colorsForCurrentTheme)
    }

    @Test
    fun whenTheSyncStatusIsPendingThenTheSyncStatusIconShouldHaveGreyTint() {
        var colorsForCurrentTheme = Colors()
        val message = Message(
            "id",
            "threadId",
            "from",
            "body",
            0L,
            "type",
            Result.Pending()
        )

        composeTestRule.setContent {
            // Initialize MainContent with fake messages
            colorsForCurrentTheme = LocalColors.current
            MainContent(
                lastMessageList =  remember { mutableStateListOf(message) },
                showErrorMessageForPermissionDenied = MutableStateFlow(false).collectAsState()
            )
        }

        assertSyncStatusIconMatchesForGivenMessage(message, colorsForCurrentTheme)
    }

    @Test
    fun givenTheSyncStatusOfTheMessageTheCorrespondingSyncStatusIconTintShouldBeUsed() {
        var colorsForCurrentTheme = Colors()
        composeTestRule.setContent {
            // Initialize MainContent with fake messages
            val rememberFakeMessages = remember { mutableStateListOf<Message>() }
            rememberFakeMessages.addAll(fakeMessages)
            colorsForCurrentTheme = LocalColors.current
            MainContent(
                lastMessageList = rememberFakeMessages,
                showErrorMessageForPermissionDenied = MutableStateFlow(false).collectAsState()
            )
        }

        fakeMessages.forEach { fakeMessage ->
            assertSyncStatusIconMatchesForGivenMessage(fakeMessage, colorsForCurrentTheme)
        }

    }

    private fun assertSyncStatusIconMatchesForGivenMessage(
        message: Message,
        colorsForCurrentTheme: Colors
    ) {
        val imageBitmapOfSyncIcon = composeTestRule.onNode(
            /** This alone returns multiple nodes as same description is used for each item **/
            hasContentDescription(context.getString(R.string.image_description_sync_status_logo))
                /** Filter down to [SyncStatusIcon] of the first message **/
                .and(hasParent(hasAnyChild(hasText(message.body)))),
            true
        ).captureToImage()

        /** Get the pixel of a non transparent area in [R.drawable.baseline_sync_status_24] icon **/
        val pixel = imageBitmapOfSyncIcon.asAndroidBitmap().getPixel(
            imageBitmapOfSyncIcon.width / 3,
            imageBitmapOfSyncIcon.height / 3
        )

        val expectedIconTintColor = when (message.syncStatus) {
            is Result.Success -> {
                colorsForCurrentTheme.success
            }
            is Result.Failure -> {
                colorsForCurrentTheme.failure
            }
            else -> {
                Color.LightGray
            }
        }

        Assert.assertEquals(
            "Failed for ${message.syncStatus}",
            expectedIconTintColor.toArgb(),
            pixel
        )
    }
}
