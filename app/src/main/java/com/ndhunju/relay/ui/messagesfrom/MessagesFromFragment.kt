package com.ndhunju.relay.ui.messagesfrom

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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.theme.RelayTheme

private const val THREAD_ID = "THREAD_ID"
private const val SENDER_ADDRESS = "SENDER_ADDRESS"

/**
 * A simple [Fragment] subclass that shows all the messages from the passed sender/address
 * Use the [MessagesFromFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFromFragment : Fragment() {

    private var threadId: String? = null
    private var senderAddress: String? = null
    private val viewModel: MessagesFromViewModel by activityViewModels {
        RelayViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            threadId = it.getString(THREAD_ID)
            senderAddress = it.getString(SENDER_ADDRESS)
        }

        viewModel.threadId = threadId ?: return
        viewModel.senderAddress = senderAddress ?: return

        // Make async call to get the data here
        threadId?.let { viewModel.getSmsByThreadId(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    // This coroutine is bound to the lifecycle of the enclosing compose
                    //val composeCoroutine = rememberCoroutineScope()
                    if (viewModel.isLoading) {
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
                        visible = viewModel.isLoading.not(),
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
                            senderAddress,
                            viewModel.messagesInThread,
                            viewModel.textMessage,
                            viewModel.onTextMessageChange,
                            viewModel::sendMessage,
                            // TODO: Nikesh - Implement proper nav controller
                            onBackPressed = { parentFragmentManager.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    companion object {

        val TAG: String = MessagesFromFragment::class.java.name

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param threadId id of message thread
         * @return A new instance of fragment MessagesFromFragment.
         */
        fun newInstance(threadId: String, senderAddress: String) =
            MessagesFromFragment().apply {
                arguments = Bundle().apply {
                    putString(THREAD_ID, threadId)
                    putString(SENDER_ADDRESS, senderAddress)
                }
            }

        /**
         * Adds this fragment to [android.R.id.content]
         */
        fun addToContent(fm: FragmentManager, message: Message) {
            fm.beginTransaction()
                .add(android.R.id.content, newInstance(message.threadId, message.from), TAG)
                .addToBackStack(TAG)
                .commit()
        }
    }
}

