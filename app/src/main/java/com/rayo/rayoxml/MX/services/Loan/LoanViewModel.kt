package com.rayo.rayoxml.mx.services.Loan

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoanRenewViewModel(private val repository: LoanRepository) : ViewModel() {
    // Datos renovacion
    private val _renewalData = MutableLiveData<LoanRenewalProposalLoadResponse?>()
    val renewalData: LiveData<LoanRenewalProposalLoadResponse?> get() = _renewalData

    fun getRenewalData(data: LoanRenewalProposalLoadRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getDataRenewalProposalLoad(data)

            // Guardar los datos del usuario
            if (data?.solicitud != null) {
                _renewalData.postValue(data)
            }
        }
    }

    fun setRenewalData(data: LoanRenewalProposalLoadResponse) {
        _renewalData.value = data
        Log.d("UserViewModel", "Asignando datos renovación: ${_renewalData.value}")
    }
}

class LoanStepOneViewModel(private val repository: LoanRepository) : ViewModel() {

    private val _userResult = MutableLiveData<LoanStepOneResponse?>()
    val userResult: LiveData<LoanStepOneResponse?> get() = _userResult

    private val _userData = MutableLiveData<Solicitud?>()
    val userData: LiveData<Solicitud?> get() = _userData  // Datos del usuario

    fun getData(loanRequestData: LoanStepOneRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getData(loanRequestData)
            _userResult.postValue(data)

            // Guardar los datos del usuario
            if (data?.solicitud != null) {
                _userData.postValue(data.solicitud)
            }
        }
    }

    // Asignar datos cuando se llama al repositorio sin usar función getData() y disparar observadores
    fun setData(data: LoanStepOneResponse) {
        _userResult.value = data
        _userData.value = data.solicitud
    }

    fun clearUserData() {
        _userData.value = null // Opción 1: Reinicia el LiveData
        // userData.postValue(null) // Opción 2: Si usas hilos secundarios
    }
}

class LoanStepTwoViewModel(private val repository: LoanRepository) : ViewModel() {

    private val _userResult = MutableLiveData<LoanStepTwoResponse?>()
    val userResult: LiveData<LoanStepTwoResponse?> get() = _userResult

    private val _userData = MutableLiveData<Solicitud?>()
    val userData: LiveData<Solicitud?> get() = _userData  // Datos del usuario

    fun getData(loanRequestData: LoanStepTwoRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getDataStepTwo(loanRequestData)
            _userResult.postValue(data)

            // Guardar los datos del usuario
            if (data?.solicitud != null) {
                _userData.postValue(data.solicitud)
            }
        }
    }
}

class LoanValidationStepViewModel(private val repository: LoanRepository) : ViewModel() {

    private val _userResult = MutableLiveData<LoanValidationResponse?>()
    val userResult: LiveData<LoanValidationResponse?> get() = _userResult

    private val _userData = MutableLiveData<Validacion?>()
    val userData: LiveData<Validacion?> get() = _userData  // Datos del usuario

    fun getData(requestData: LoanValidationRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getDataValidationStep(requestData)
            _userResult.postValue(data)

            // Guardar los datos del usuario
            if (data?.solicitud != null) {
                _userData.postValue(data.solicitud)
            }
        }
    }

    // Asignar datos cuando se llama al repositorio sin usar función getData() y disparar observadores
    fun setData(data: LoanValidationResponse) {
        _userResult.value = data
        _userData.value = data.solicitud
    }
}

// Clase independiente
class LoanStepOneViewModelFactory(private val repository: LoanRepository,  private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanStepOneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoanStepOneViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LoanStepTwoViewModelFactory(private val repository: LoanRepository, private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanStepTwoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoanStepTwoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LoanValidationStepViewModelFactory(private val repository: LoanRepository, private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanValidationStepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoanValidationStepViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Renovación
class LoanRenewalViewModelFactory(private val repository: LoanRepository,  private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanRenewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoanRenewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}