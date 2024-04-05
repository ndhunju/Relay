package com.ndhunju.relay.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.BaseFragment

/**
 * Fragment that will hold settings that would help with debugging issues, if any, in the app
 */
class DebugFragment: BaseFragment() {

    private val debugViewModel: DebugViewModel by viewModels { RelayViewModelFactory }

    override fun onCreateChildView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DebugScreen(debugViewModel.onClickForceCrashItem) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    companion object {
        val TAG: String = DebugFragment::class.java.simpleName

        fun newFragment(): DebugFragment {
            return DebugFragment()
        }

        /**
         * Adds this fragment to [android.R.id.content]
         */
        fun addToContent(fm: FragmentManager) {
            fm.beginTransaction()
                .add(android.R.id.content, newFragment(), TAG)
                .addToBackStack(TAG)
                .commit()
        }
    }
}