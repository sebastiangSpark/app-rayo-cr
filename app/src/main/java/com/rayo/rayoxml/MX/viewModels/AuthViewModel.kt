package com.rayo.rayoxml.mx.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayo.rayoxml.co.models.Country
import com.rayo.rayoxml.co.models.LoanCard
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.utils.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

class AuthViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    val selectedCountry: StateFlow<Country?> = _selectedCountry.asStateFlow()

    private val _selectedImage = MutableStateFlow<Int?>(null)
    val selectedImage: StateFlow<Int?> = _selectedImage.asStateFlow()

    private val _loanCard = MutableStateFlow<LoanCard?>(null)
    val loanCard: StateFlow<LoanCard?> = _loanCard.asStateFlow()

    init {
        observeLoginStatus()
        loadUserData()
        loadSelectedCountry()
        loadSelectedImage()
        loadLoanCard()
    }

    private fun observeLoginStatus() {
        viewModelScope.launch {
            preferencesManager.isLoggedIn().collect { loggedIn ->
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    loadUserData()
                } else {
                    _user.value = null
                }
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            preferencesManager.getUserData().collect { userData ->
                _user.value = userData
            }
        }
    }

    private fun loadSelectedCountry() {
        viewModelScope.launch {
            preferencesManager.getSelectedCountry().collect { country ->
                _selectedCountry.value = country
            }
        }
    }

    fun saveSelectedCountry(country: Country) {
        viewModelScope.launch {
            preferencesManager.saveSelectedCountry(country)
            _selectedCountry.value = country // Update UI instantly
        }
    }

    private fun loadSelectedImage() {
        viewModelScope.launch {
            preferencesManager.selectedImage.collect { imageResId ->
                _selectedImage.value = imageResId
            }
        }
    }

    fun saveSelectedImage(imageResId: Int) {
        viewModelScope.launch {
            preferencesManager.saveSelectedImage(imageResId)
            _selectedImage.value = imageResId // Update UI instantly
        }
    }

    private fun loadLoanCard() {
        viewModelScope.launch {
            preferencesManager.getLoanCard().collect { loan ->
                _loanCard.value = loan
            }
        }
    }

    fun saveLoanCard(loanCard: LoanCard) {
        viewModelScope.launch {
            preferencesManager.saveLoanCard(loanCard)
            _loanCard.value = loanCard // Update UI instantly
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.logout()
            _isLoggedIn.value = false
            _user.value = null
            _selectedImage.value = null
            _loanCard.value = null
        }
    }
}


class AuthViewModelFactory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
