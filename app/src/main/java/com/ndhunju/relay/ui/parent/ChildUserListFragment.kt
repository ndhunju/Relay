package com.ndhunju.relay.ui.parent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.theme.RelayTheme

/**
 * A [Fragment] subclass that shows list of paired child users
 * Use the [ChildUserListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildUserListFragment : Fragment() {

    private val viewModel: ChildUserListViewModel by viewModels { RelayViewModelFactory }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    ChildUserListScreen(
                        viewModel = viewModel,
                        onUpPressed = { parentFragmentManager.popBackStack() }
                    )
                }
            }
        }
    }

    companion object {

        val TAG: String = ChildUserListFragment::class.java.name
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ChildUserListFragment.
         */
        @JvmStatic
        fun newInstance() = ChildUserListFragment()
    }
}