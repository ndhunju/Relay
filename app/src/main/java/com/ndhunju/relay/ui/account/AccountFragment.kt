package com.ndhunju.relay.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.theme.RelayTheme

class AccountFragment: Fragment() {

    private val accountViewModel: AccountViewModel by viewModels { RelayViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    val uiState = accountViewModel.state.collectAsStateWithLifecycle()
                    AccountScreen(
                        accountScreenUiState = uiState.value,
                        onEmailChange = accountViewModel.onEmailChange,
                        onNameChange = accountViewModel.onNameChange,
                        onPhoneChange = accountViewModel.onPhoneChange,
                        onClickCreateUpdate = accountViewModel.onClickCreateUpdateUser,
                        onUpPressed = { parentFragmentManager.popBackStack() }
                    )
                }
            }
        }
    }

    companion object {

        val TAG: String = AccountFragment::class.java.name

        /**
         * Use this factory method to create a new instance of this fragment
         *
         * @return A new instance of fragment [AccountFragment].
         */
        fun newInstance() = AccountFragment()
    }
}