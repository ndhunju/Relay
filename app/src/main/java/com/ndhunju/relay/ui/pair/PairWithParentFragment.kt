package com.ndhunju.relay.ui.pair

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.BaseFragment
import com.ndhunju.relay.ui.theme.RelayTheme

/**
 * Fragment that holds the view for letting user pair this app with a parent user.
 * Once paired with a parent user, app will start sending new messages to that parent user.
 */
class PairWithParentFragment: BaseFragment() {

    private val viewModel: PairWithParentViewModel by viewModels { RelayViewModelFactory }

    override fun onCreateChildView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    PairWithParentScreen(
                        viewModel.pairedUserPhoneList.collectAsState(),
                        viewModel.selectedParentPhoneAddress.collectAsState(),
                        viewModel.isSelectedParentPaired.collectAsState(),
                        viewModel.showProgress.collectAsState(),
                        viewModel.errorMsgResId.collectAsState(),
                        onUpPressed = { parentFragmentManager.popBackStack() },
                        onClickPairUnPair = viewModel::onClickPairUnpair,
                        onClickPairedUser = viewModel::onClickPairedUser,
                        onParentPhoneChanged = viewModel::onSelectedParentPhoneChanged
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

        /**
         * Adds this fragment to [android.R.id.content]
         */
        fun addToContent(fm: FragmentManager) {
            fm.beginTransaction()
                .add(android.R.id.content, newInstance(), TAG)
                .addToBackStack(TAG)
                .commit()
        }
    }
}