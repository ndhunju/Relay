package com.ndhunju.relay.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.ndhunju.relay.R
import com.ndhunju.relay.util.connectivity.LollipopNetworkConnectionChecker
import com.ndhunju.relay.util.connectivity.NetworkConnectionChecker
import com.ndhunju.relay.util.connectivity.NougatNetworkConnectionChecker

open class BaseActivity: FragmentActivity() {

    private lateinit var networkConnectionChecker: NetworkConnectionChecker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNetworkConnectionChecker()
    }

    /**
     * Initializes [networkConnectionChecker] with appropriate implementation
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun initNetworkConnectionChecker() {
        networkConnectionChecker = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NougatNetworkConnectionChecker(this)
        } else {
            LollipopNetworkConnectionChecker(this)
        }

        networkConnectionChecker.observe(this) { isOnline ->
            if (isOnline.not()) {
                Toast.makeText(
                    this,
                    getString(R.string.alert_offline_body),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}