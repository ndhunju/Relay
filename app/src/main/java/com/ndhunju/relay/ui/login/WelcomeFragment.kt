package com.ndhunju.relay.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.BaseFragment
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.theme.RelayTheme

class WelcomeFragment: BaseFragment() {

    override fun onCreateChildView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    WelcomeScreen(onClickNext = {
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.enter_from_right,
                                R.anim.exit_to_left,
                                R.anim.enter_from_left,
                                R.anim.exit_to_right
                            )
                            .replace(android.R.id.content, AccountFragment.newInstance())
                            .addToBackStack(AccountFragment.TAG)
                            .commit()
                    })
                }
            }
        }
    }
}