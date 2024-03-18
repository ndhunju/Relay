package com.ndhunju.relay.ui.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ndhunju.relay.R
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.ui.custom.SyncStatusIcon
import com.ndhunju.relay.ui.mockMessages
import com.ndhunju.relay.ui.theme.LocalDimens
import com.ndhunju.relay.util.dateFormat
import com.ndhunju.relay.util.extensions.getColorForId

@Preview(showBackground = true)
@Composable
fun MessageListItemPreview() {
    MessageListItem(
        message = mockMessages.first().copy(syncStatus = Result.Success()),
        onClick = {}
    )
}

@Composable
fun MessageListItem(
    modifier: Modifier = Modifier,
    message: Message,
    onClick: ((Message) -> Unit)? = null
) {
    ConstraintLayout(modifier = modifier
        .clickable { onClick?.invoke(message) }
    ) {
        val (divider, profilePic, from, body, date, status) = createRefs()
        val itemVerticalPadding = LocalDimens.current.itemPaddingVertical
        val contentHorizontalPadding = LocalDimens.current.contentPaddingHorizontal

        //LogCompositions(tag = "MessageListItem", msg = "MessageListItem scope")

        Divider(Modifier.constrainAs(divider) {
            top.linkTo(parent.top)
            width = Dimension.fillToConstraints
        })

        Image(
            modifier = Modifier.constrainAs(profilePic) {
                top.linkTo(parent.top, itemVerticalPadding)
                bottom.linkTo(parent.bottom, itemVerticalPadding)
                start.linkTo(parent.start, contentHorizontalPadding.div(2))
                width = Dimension.value(50.dp)
                height = Dimension.value(50.dp)
            },
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(id = R.string.image_description_user),
            colorFilter = ColorFilter.tint(
                LocalContext.current.getColorForId(message.from.hashCode())
            ),
        )

        Text(text = message.from, Modifier.constrainAs(from) {
            top.linkTo(parent.top, itemVerticalPadding)
            start.linkTo(profilePic.end, contentHorizontalPadding.div(2))
            width = Dimension.fillToConstraints
        },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )


        Text(
            text = message.getFormattedTime(),
            Modifier.constrainAs(date) {
                linkTo(
                    start = from.end,
                    end = parent.end,
                    startMargin = 4.dp,
                    endMargin = contentHorizontalPadding,
                    bias = 1f,
                )
                top.linkTo(parent.top, itemVerticalPadding)
            },
            style = MaterialTheme.typography.labelMedium
        )

        SyncStatusIcon(
            syncStatus = message.syncStatus,
            modifier = Modifier
                .constrainAs(status) {
                    end.linkTo(parent.end, contentHorizontalPadding)
                    top.linkTo(date.bottom, 4.dp)
                    bottom.linkTo(parent.bottom, itemVerticalPadding)
                }
        )

        Text(
            text = message.body, // + "\n" + message.toString(), // for debugging
            Modifier
                .constrainAs(body) {
                    top.linkTo(from.bottom)
                    linkTo(
                        start = profilePic.end,
                        end = status.start,
                        startMargin = contentHorizontalPadding.div(2),
                        endMargin = 8.dp,
                        bias = 0f,
                    )
                    bottom.linkTo(parent.bottom, itemVerticalPadding)
                    width = Dimension.fillToConstraints
                },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

data class Message(
    /**
     * Id or PK that is stored in the SMS database in the device by OS
     */
    val idInAndroidDb: String,
    val threadId: String,
    val from: String,
    val body: String,
    val date: Long,
    val type: String,
    /**
     * Null means this instance of the Message/Sms was sent before our app was installed.
     * So we never pushed is to the cloud database. In terms of UI, we should hide the sync icon.
     * **/
    var syncStatus: Result<Void>? = null,
    val extra: String? = null
) {
    fun getFormattedTime(): String {
        val dateAsLong = date
        return dateFormat.format(dateAsLong)
    }

    fun isSentByUser(): Boolean {
        return type == "2"
    }
}