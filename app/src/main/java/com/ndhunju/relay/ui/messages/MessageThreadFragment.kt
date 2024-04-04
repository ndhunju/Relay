package com.ndhunju.relay.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.ndhunju.relay.RelayViewModelFactory
import com.ndhunju.relay.ui.BaseFragment
import com.ndhunju.relay.ui.MainScreen
import com.ndhunju.relay.ui.MainViewModel
import com.ndhunju.relay.ui.theme.RelayTheme
import com.ndhunju.relay.util.areSmsPermissionGranted
import com.ndhunju.relay.util.checkIfSmsPermissionsGranted
import com.ndhunju.relay.util.requestSmsPermission

class MessageThreadFragment: BaseFragment() {

    private val viewModel: MainViewModel by activityViewModels { RelayViewModelFactory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (areSmsPermissionGranted(permissions)) {
            viewModel.onSmsPermissionGranted()
        } else {
            // Permissions denied
            viewModel.onSmsPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.doRequestSmsPermission = {
            requestSmsPermission(requestPermissionLauncher)
        }

        // Check if SMS read and send permissions are granted
        if (checkIfSmsPermissionsGranted(requireContext())) {
            viewModel.onSmsPermissionGranted()
        } else {
            // if (shouldShowRequestPermissionRationale(this, READ_SMS))
            // Always show the rationale
            viewModel.onSmsPermissionDenied()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                RelayTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(viewModel)
                    }
                }
            }
        }
    }

    companion object {

        val TAG: String = MessageThreadFragment::class.java.simpleName

        fun addToContent(fm: FragmentManager) {
            fm.beginTransaction()
                .add(android.R.id.content, MessageThreadFragment(), TAG)
                .commit()
        }
    }
}