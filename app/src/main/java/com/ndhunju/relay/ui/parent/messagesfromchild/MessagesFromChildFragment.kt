package com.ndhunju.relay.ui.parent.messagesfromchild

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.MainContent
import com.ndhunju.relay.ui.theme.RelayTheme

private const val ARG_CHILD_USER_ID = "ARG_CHILD_USER_ID"
private const val ARG_CHILD_USER_EMAIL = "ARG_CHILD_USER_EMAIL"

/**
 * A simple [Fragment] subclass.
 * Use the [MessagesFromChildFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFromChildFragment : Fragment() {

    private val viewModel: MessagesFromChildViewModel by viewModels { RelayViewModelFactory }
    private var childUserId: String? = null
    private var childUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            childUserId = it.getString(ARG_CHILD_USER_ID)
            childUserEmail = it.getString(ARG_CHILD_USER_EMAIL)
        }

        viewModel.doOpenMessageFromFragment = {
            // TODO: Nikesh - Open screen to show messages
        }

        viewModel.childUserEmail = childUserEmail
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    MainContent(
                        viewState = viewModel.state.collectAsStateWithLifecycle().value,
                        onClickSearchIcon = viewModel.onClickSearchIcon,
                        onSearchTextChanged = viewModel.onSearchTextChanged,
                        onClickGrantPermission = {},
                        onClickMessage = viewModel.onClickMessage
                    ) {

                    }
                }
            }
        }
    }

    companion object {

        val TAG: String = MessagesFromChildFragment::class.java.name
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MessagesFromChildFragment.
         */
        @JvmStatic
        fun newInstance(childUserId: String, childUserEmail: String) =
            MessagesFromChildFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHILD_USER_ID, childUserId)
                    putString(ARG_CHILD_USER_EMAIL, childUserEmail)
                }
            }
    }
}