package com.ndhunju.relay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras

val RelayViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Get the Application object from extras
        val application = checkNotNull(extras[APPLICATION_KEY]) as RelayApplication
        val repository = application.appComponent.relayRepository()
        val cloudDatabaseService = application.appComponent.cloudDatabaseService()
        val smsRepository = application.appComponent.smsInfoRepository()
        with(modelClass) {
            return when {
                isAssignableFrom(RelaySmsViewModel::class.java) -> {
                    RelaySmsViewModel(repository, smsRepository, cloudDatabaseService) as T
                }
                else -> throw IllegalArgumentException(
                    "Unknown ViewModel class: ${modelClass.name}"
                )
            }
        }

    }
}
