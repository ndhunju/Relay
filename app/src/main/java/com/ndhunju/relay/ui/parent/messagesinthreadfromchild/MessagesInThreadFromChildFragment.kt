package com.ndhunju.relay.ui.parent.messagesinthreadfromchild

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.BaseFragment
import com.ndhunju.relay.ui.messagesfrom.MessagesFromFragment
import com.ndhunju.relay.ui.messagesfrom.MessagesFromView
import com.ndhunju.relay.ui.theme.RelayTheme

/// Constants used as keys for Bundle
private const val THREAD_ID = "THREAD_ID"
private const val CHILD_USER_ID = "CHILD_USER_ID"
private const val SENDER_ADDRESS = "SENDER_ADDRESS"

/**
 * A simple [Fragment] subclass that shows all the messages from the passed arguments.
 * Use the [MessagesFromFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class MessagesInThreadFromChildFragment: BaseFragment() {

    private val viewModel: MessagesInThreadFromChildVM by viewModels { RelayViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = arguments ?: return
        val threadId = arguments.getString(THREAD_ID) ?: return
        val childUserId = arguments.getString(CHILD_USER_ID) ?: return
        val senderAddress = arguments.getString(SENDER_ADDRESS) ?: return
        viewModel.senderAddress = senderAddress
        viewModel.loadMessagesForChildAndThread(childUserId, threadId)
    }

    override fun onCreateChildView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    val uiState = viewModel.messageFromUiState.collectAsStateWithLifecycle()
                    // Show progress if loading
                    if (uiState.value.isLoading.value) {
                        Box(modifier = Modifier
                            .wrapContentSize(align = Alignment.Center)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(32.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.value.isLoading.value.not(),
                        enter = slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = LinearOutSlowInEasing
                            )
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        )
                    ) {
                        MessagesFromView(
                            viewModel.senderAddress,
                            uiState.value.messagesInThread,
                            uiState.value.isLoading.value,
                            onClickSend = {
                                showDialog(context.getString(
                                    R.string.unsupported_action_sending_message
                                ))
                                analyticsProvider.logEvent("didClickOnSendMessageToChild")
                                          },
                            onBackPressed = { parentFragmentManager.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    companion object {

        val TAG: String = MessagesInThreadFromChildFragment::class.java.name

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param threadId id of message thread
         * @return A new instance of fragment MessagesInThreadFromChildFragment.
         */
        fun newInstance(childUserId: String, threadId: String, senderAddress: String) =
            MessagesInThreadFromChildFragment().apply {
                arguments = Bundle().apply {
                    putString(THREAD_ID, threadId)
                    putString(CHILD_USER_ID, childUserId)
                    putString(SENDER_ADDRESS, senderAddress)
                }
            }
    }

}