package com.ndhunju.relay.ui.account

import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ndhunju.relay.R
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.BaseFragment
import com.ndhunju.relay.ui.custom.MessageAlertDialog
import com.ndhunju.relay.ui.theme.RelayTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountFragment: BaseFragment() {

    private val accountViewModel: AccountAndroidViewModel by viewModels { RelayViewModelFactory }

    override fun onCreateChildView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply {
            addView(ComposeView(requireContext()).apply {
                setContent {
                    RelayTheme {
                        val uiState = accountViewModel.instance.state.collectAsStateWithLifecycle()
                        AccountScreen(
                            accountScreenUiState = uiState.value,
                            // Show UP button only when there are other fragments in the backstack
                            showUpButton = parentFragmentManager.backStackEntryCount > 0,
                            onNameChange = accountViewModel.instance.onNameChange,
                            onPhoneChange = accountViewModel.instance.onPhoneChange,
                            onEncKeyChange = accountViewModel.instance.onEncKeyChange,
                            onClickCreateUpdate = accountViewModel.instance.onClickCreateUpdateUser,
                            onUpPressed = { parentFragmentManager.popBackStack() },
                            onClickDialogBtnOk = accountViewModel.instance.onClickDialogBtnOk
                        )
                    }
                }
            },
                LayoutParams(MATCH_PARENT, MATCH_PARENT)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showAccountUnverifiedDialogIfNotified()
    }

    private fun showAccountUnverifiedDialogIfNotified() {
        lifecycleScope.launch {
            var dialog: View? = null
            accountViewModel.instance.showAccountUnverifiedDialog.collectLatest { show ->
                if (show) {
                    (view as ViewGroup).apply {
                        dialog = ComposeView(requireContext()).apply {
                            setContent { AccountVerificationDialog() }
                        }
                        addView(dialog)
                    }
                } else {
                    if (dialog != null) {
                        (view as ViewGroup).removeView(dialog)
                    }
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

    @Composable
    fun AccountVerificationDialog() {
        RelayTheme {
            MessageAlertDialog(
                stringResource(R.string.account_verification_dialog_message),
                onClickDialogBtnCancel = accountViewModel.instance.onClickAccountUnverifiedDialogBtn,
                onClickDialogBtnOk = accountViewModel.instance.onClickAccountUnverifiedDialogBtn
            )
        }
    }
}