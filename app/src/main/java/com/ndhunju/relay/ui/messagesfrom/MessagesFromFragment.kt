package com.ndhunju.relay.ui.messagesfrom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.getSmsByThreadId

private const val THREAD_ID = "THREAD_ID"

/**
 * A simple [Fragment] subclass that shows all the messages from the passed sender/address
 * Use the [MessagesFromFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFromFragment : Fragment() {

    private var threadId: String? = null

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
                    // Process the data
                    val threadId: String = threadId ?: ""
                    val messages = if (threadId.isNotEmpty()) {
                        getSmsByThreadId(
                            context.contentResolver,
                            threadId
                        )
                    } else {
                        emptyList()
                    }

                    MessagesFromView(
                        messages.first().from,
                        messages,
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

