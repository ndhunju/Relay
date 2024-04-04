package com.ndhunju.relay.ui.parent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.pair.AddChildEncryptionKeyFromQrCodeActivity
import com.ndhunju.relay.ui.parent.messagesfromchild.MessagesFromChildFragment
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.isNotificationPermissionGranted
import com.ndhunju.relay.util.requestNotificationPermission

/**
 * A [Fragment] subclass that shows list of paired child users
 * Use the [ChildUserListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChildUserListFragment : Fragment() {

    private val viewModel: ChildUserListViewModel by viewModels { RelayViewModelFactory }

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.invalidateChildUsers()
        }

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (isNotificationPermissionGranted(permissions).not()) {
            viewModel.onDeniedNotificationPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.doOpenMessagesFromChildFragment = { child ->
            parentFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    MessagesFromChildFragment.newInstance(child.id, child.phone)
                )
                .addToBackStack(MessagesFromChildFragment.TAG)
                .commit()
        }

        viewModel.doOpenAddChildEncryptionKeyFromQrCodeFragment = { _: Child ->
            activityLauncher.launch(
                Intent(context, AddChildEncryptionKeyFromQrCodeActivity::class.java)
            )
        }

        viewModel.doRequestNotificationPermission = {
            requestNotificationPermission(requestPermissionLauncher)
        }
    }

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