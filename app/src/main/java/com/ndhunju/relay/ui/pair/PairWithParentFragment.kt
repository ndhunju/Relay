package com.ndhunju.relay.ui.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.theme.RelayTheme

/**
 * Fragment that holds the view for letting user pair this app with a parent user.
 * Once paired with a parent user, app will start sending new messages to that parent user.
 */
class PairWithParentFragment: Fragment() {

    private val viewModel: PairWithParentViewModel by viewModels { RelayViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    PairWithParentScreen(
                        viewModel.parentEmailAddress,
                        viewModel.onParentEmailAddressChanged,
                        viewModel.showProgress,
                        viewModel.onClickPair,
                        onUpPressed = { parentFragmentManager.popBackStack() }
                    )
                }
            }
        }
    }

    companion object {

        val TAG: String = PairWithParentFragment::class.java.name

        /**
         * Use this factory method to create a new instance of
         * this fragment
         */
        fun newInstance() = PairWithParentFragment()
    }
}