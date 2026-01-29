package com.rayo.rayoxml.co.services.Payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.utils.PreferencesManager

class PaymentViewModel(
    private val repository: PaymentRepository,
    private val preferencesManager: PreferencesManager // Add PreferencesManager
) : ViewModel() {

}

// Model Factory
class PaymentViewModelFactory(
    private val repository: PaymentRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}