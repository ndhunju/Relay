package com.ndhunju.relay.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ndhunju.relay.RelayViewModelFactory

/**
 * Fragment that will hold settings that would help with debugging issues, if any, in the app
 */
class DebugFragment: Fragment() {

    private val debugViewModel: DebugViewModel by viewModels { RelayViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
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
    }
}