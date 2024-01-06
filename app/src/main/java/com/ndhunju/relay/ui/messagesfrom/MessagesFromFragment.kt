package com.ndhunju.relay.ui.messagesfrom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.getSmsBySender

private const val SENDER_ADDRESS = "SENDER_ADDRESS"

/**
 * A simple [Fragment] subclass that shows all the messages from the passed sender/address
 * Use the [MessagesFromFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFromFragment : Fragment() {

    private var senderAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            senderAddress = it.getString(SENDER_ADDRESS)
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
                    val senderAddress: String = senderAddress ?: ""
                    val messages = if (senderAddress.isNotEmpty()) {
                        getSmsBySender(
                            context.contentResolver,
                            senderAddress
                        )
                    } else {
                        emptyList()
                    }

                    MessagesFromView(
                        senderAddress,
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
         * @param senderAddress address of sender. Eg 408 320 4832.
         * @return A new instance of fragment MessagesFromFragment.
         */
        @JvmStatic
        fun newInstance(senderAddress: String) =
            MessagesFromFragment().apply {
                arguments = Bundle().apply {
                    putString(SENDER_ADDRESS, senderAddress)
                }
            }
    }
}

