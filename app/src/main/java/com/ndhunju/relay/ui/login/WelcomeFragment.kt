package com.ndhunju.relay.ui.login

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ndhunju.relay.ui.account.AccountFragment
import com.ndhunju.relay.ui.theme.RelayTheme

class WelcomeFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    WelcomeScreen(onClickNext = {
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                com.ndhunju.relay.R.anim.enter_from_right,
                                com.ndhunju.relay.R.anim.exit_to_left,
                                com.ndhunju.relay.R.anim.enter_from_left,
                                com.ndhunju.relay.R.anim.exit_to_right
                            )
                            .replace(R.id.content, AccountFragment.newInstance())
                            .addToBackStack(AccountFragment.TAG)
                            .commit()
                    })
                }
            }
        }
    }
}