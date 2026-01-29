package com.rayo.rayoxml.cr.services.User

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rayo.rayoxml.cr.services.Auth.AuthRepository
import com.rayo.rayoxml.cr.services.Auth.LoginResponse
import com.rayo.rayoxml.cr.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.cr.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    private val preferencesManager: PreferencesManager // Add PreferencesManager
) : ViewModel() {

    // Nombre de usuario (CR)
    private val _userFullName = MutableLiveData<String>()
    val userFullName: LiveData<String?> get() = _userFullName

    fun setUserFullName(name: String) {
        _userFullName.value = name
    }

    // Para datos de usuario
    private val authRepository: AuthRepository = AuthRepository()

    private val _userResult = MutableLiveData<LoginResponse?>()
    val userResult: LiveData<LoginResponse?> get() = _userResult

    private val _userData = MutableLiveData<LoginResponse?>()
    val userData: LiveData<LoginResponse?> get() = _userData  // Datos del usuario

    fun getData(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authRepository.loginUserWithId(contactId)
            _userResult.postValue(data)
            _userData.postValue(data)

            // Guardar los datos del usuario
            if (data != null) {
                setData(data)
            }
        }
    }

    // Guardar datos de usuario
    fun setData(data: LoginResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            _userData.postValue(data)

            try {
                // Guardar datos de sesión
                preferencesManager.saveUserData(data.id, data.nombre, data.email)
            } catch (e: Exception) {
                println("❗ Error: ${e.message}")
            }
            Log.d("UserViewModel CR", "Asignando datos personales: ${_userData.value}")
        }
    }

    // Formulario datos personales
    //private val _personalData = MutableLiveData<LoanStepOneRequest?>()
    private val _personalData = MutableLiveData(LoanStepOneRequest(
        nombre = "",
        primerApellido = "",
        numeroDocumento = "",
        fechaNacimiento = "",
        celular = "",
        empleadoFormal = "",
        nombreCuentaBancaria = "",
        correoElectronico = "",
        tipoDocumento = "",
        plazoSeleccionado = 0,
        valorSeleccionado = 0,
        //ingresoMensual = 0,
        fechaExpedicion = "",
        sessionId = "",
        genero = ""
    )) // Default empty instance
    val personalData: LiveData<LoanStepOneRequest?> get() = _personalData
    // Formulario datos bancarios
    private val _bankData = MutableLiveData<LoanStepTwoRequest?>()
    val bankdData: LiveData<LoanStepTwoRequest?> get() = _bankData

    fun setPersonalData(data: LoanStepOneRequest) {
        //_personalData.value = data
        val existingData = _personalData.value

        // Preserve the previous `plazoSeleccionado` if it was set before
        _personalData.value = data.copy(
            plazoSeleccionado = existingData?.plazoSeleccionado ?: data.plazoSeleccionado,
            valorSeleccionado = existingData?.valorSeleccionado ?: data.valorSeleccionado
        )
        Log.d("UserViewModel CR", "Asignando datos personales: ${_personalData.value}")
    }

    fun setBankData(data: LoanStepTwoRequest) {
        _bankData.value = data
    }

    fun updateLoanTerm(plazo: Int) {
        val currentUserData = _personalData.value
        _personalData.value = currentUserData?.copy(plazoSeleccionado = plazo)

        Log.d("UserViewModel CR", "Datos actualizados: ${_personalData.value}")
    }

    fun updateLoanAmount(amount: Int) {
        val currentUserData = _personalData.value
        _personalData.value = currentUserData?.copy(valorSeleccionado = amount)
        Log.d("UserViewModel CR", "Datos actualizados: ${_personalData.value}")
    }

    // Datos adicionales
    private var _formId = MutableLiveData<String?>()
    val formId: LiveData<String?> get() = _formId
    private var _showTumiPay = MutableLiveData<Boolean?>()
    val showTumiPay: LiveData<Boolean?> get() = _showTumiPay

    fun setFormId(formId: String) {
        _formId.value = formId
    }

    fun setTumiPay(option: Boolean) {
        _showTumiPay.value = option
    }

    fun clearData() {
        _userData.value = null // Si usas MutableLiveData
        _userData.postValue(null) // Si quieres asegurar el cambio en otro hilo
    }
}

// Clase independiente
class UserViewModelFactory(
    private val repository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}