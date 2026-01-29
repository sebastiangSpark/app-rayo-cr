package com.rayo.rayoxml.co.services.User

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rayo.rayoxml.co.models.PaymentDate
import com.rayo.rayoxml.co.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    private val preferencesManager: PreferencesManager // Add PreferencesManager
) : ViewModel() {

    private val _userResult = MutableLiveData<UserResponse?>()
    val userResult: LiveData<UserResponse?> get() = _userResult

    private val _userData = MutableLiveData<Solicitud?>()
    val userData: LiveData<Solicitud?> get() = _userData  // Datos del usuario

    fun getData(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getData(contactId)
            _userResult.postValue(data)
            Log.d("ViewModel", "User data updated")

            // Guardar los datos del usuario
            if (data?.solicitud != null) {
                _userData.postValue(data.solicitud)

                try {
                    // Guardar datos de sesión
                    preferencesManager.saveUserData(contactId, data.solicitud.nombre!!, data.solicitud.email!!)
                } catch (e: Exception) {
                    println("❗ Error: ${e.message}")
                }
            }
        }
    }

    // Formulario datos personales
    //private val _personalData = MutableLiveData<LoanStepOneRequest?>()
    private var _personalData = MutableLiveData(LoanStepOneRequest(
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
        ingresoMensual = 0,
        fechaExpedicion = "",
        sessionId = "",
        genero = ""
    )) // Default empty instance
    val personalData: LiveData<LoanStepOneRequest?> get() = _personalData

    fun setPersonalData(data: LoanStepOneRequest) {
        //_personalData.value = data
        val existingData = _personalData.value

        // Preserve the previous `plazoSeleccionado` if it was set before
        _personalData.value = data.copy(
            plazoSeleccionado = existingData?.plazoSeleccionado ?: data.plazoSeleccionado,
            valorSeleccionado = existingData?.valorSeleccionado ?: data.valorSeleccionado
        )
        Log.d("UserViewModel", "Asignando datos personales: ${_personalData.value}")
    }

    // Formulario datos bancarios
    private val _bankData = MutableLiveData<LoanStepTwoRequest?>()
    val bankdData: LiveData<LoanStepTwoRequest?> get() = _bankData

    fun setBankData(data: LoanStepTwoRequest) {
        _bankData.value = data
    }

    fun updateLoanTerm(plazo: Int) {
        val currentUserData = _personalData.value
        _personalData.value = currentUserData?.copy(plazoSeleccionado = plazo)

        Log.d("UserViewModel", "Datos actualizados: ${_personalData.value}")
    }

    fun updateLoanAmount(amount: Int) {
        val currentUserData = _personalData.value
        _personalData.value = currentUserData?.copy(valorSeleccionado = amount)
        Log.d("UserViewModel", "Datos actualizados: ${_personalData.value}")
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

    // Fechas de pagos
    private val _paymentDates = MutableLiveData<MutableList<PaymentDate>>(mutableListOf())
    val paymentDates: LiveData<MutableList<PaymentDate>> get() = _paymentDates

    fun setPaymentDates(dates: MutableList<PaymentDate>) {
        _paymentDates.value = dates
    }

    // Montos
    private val _loanTermsValues = MutableLiveData<MutableList<String>>(mutableListOf())
    val loanTermsValues: LiveData<MutableList<String>> get() = _loanTermsValues

    fun setLoanTermsValues(data: MutableList<String>) {
        _loanTermsValues.value = data
    }

    /*private var _phone = MutableLiveData<String?>()
    val phone: LiveData<String?> get() = _phone
    private var _loanTerm = MutableLiveData<String?>()
    val loanTerm: LiveData<String?> get() = _loanTerm*/

    /*fun setPhone(data: String) {
        _phone.value = data
        Log.d("UserViewModel", "Celular actualizado: ${_phone.value}")
    }
    fun setLoanTerm(data: String) {
        _loanTerm.value = data
    }*/

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