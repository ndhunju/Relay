package com.ndhunju.relay.ui.messagesfrom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.RelaySmsViewModel
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.theme.RelayTheme
import kotlinx.coroutines.launch

private const val THREAD_ID = "THREAD_ID"

/**
 * A simple [Fragment] subclass that shows all the messages from the passed sender/address
 * Use the [MessagesFromFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFromFragment : Fragment() {

    private var threadId: String? = null
    private val relaySmsViewModel: RelaySmsViewModel by viewModels { RelayViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            threadId = it.getString(THREAD_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    val uiState = relaySmsViewModel.messageFromUiState.collectAsStateWithLifecycle()
                    // This coroutine is bound to the lifecycle of the enclosing compose
                    val composeCoroutine = rememberCoroutineScope()
                    // Process the data
                    val threadId: String = threadId ?: ""
                    if (threadId.isNotEmpty()) {
                        LaunchedEffect(key1 = "getSmsByThreadId", block = {
                            composeCoroutine.launch {
                                relaySmsViewModel.getSmsByThreadId(threadId)
                            }
                        })
                    }

                    MessagesFromView(
                        uiState.value.messagesInThread.firstOrNull()?.from ?: "",
                        uiState.value.messagesInThread,
                        // TODO: Nikesh - Implement proper nav controller
                        onBackPressed = { parentFragmentManager.popBackStack() }
                    )
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
        @JvmStatic
        fun newInstance(threadId: String) =
            MessagesFromFragment().apply {
                arguments = Bundle().apply {
                    putString(THREAD_ID, threadId)
                }
            }
    }
}

