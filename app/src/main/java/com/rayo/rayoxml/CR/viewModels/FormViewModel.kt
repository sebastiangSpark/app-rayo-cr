package com.rayo.rayoxml.cr.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rayo.rayoxml.cr.services.Loan.CreateLoanRequest
import com.rayo.rayoxml.cr.services.Loan.CreateLoanResponse

class FormViewModel : ViewModel() {

    // Crear Préstamo
    var _loanRequest = MutableLiveData(CreateLoanRequest(
        nombreCompleto = "",
        apellidos = "",
        numeroCedula = "",
        correoElectronico = "",
        numeroCelular = "",
        estadoCivil = "",
        fechaNacimiento = "",
        fechaExpiracionCedula = "",
        genero = "",
        cuentaOrderPatronal = "",
        cuentaIBAN = "",
        moneda = "",
        pais = "",
        provincia = "",
        canton = "",
        distrito = "",
        direccionExacta = "",
        salarioMensual = 0,
        fidelidad = "",
        actividadEconomica = "",
        profesion = "",
        password = "",
        cuentaOrdenPatronal = "Si",
        referido = "No",
        montoSolicitado = "40000", // Traer de simulador
        plazo = "30", // Traer de simulador
        // Datos no enviados
        referidoNombre = "",
        referidoCedula = "",
        lugarTrabajo = "Costa Rica",
        estado = "ABANDONADO",
        partner = "clearpier",
        clickid = "123456"
    ))

    val loanRequest: LiveData<CreateLoanRequest> get() = _loanRequest

    fun updatePersonalInfo(
        nombreCompleto: String,
        apellidos: String,
        numeroCedula: String,
        fechaExpiracionCedula: String,
        fechaNacimiento: String,
        genero: String,
        estadoCivil: String,
        celular: String,
        correo: String,
        clave: String
    ) {
        _loanRequest.value = _loanRequest.value?.copy(
            nombreCompleto = nombreCompleto,
            apellidos = apellidos,
            numeroCedula = numeroCedula,
            fechaExpiracionCedula = fechaExpiracionCedula,
            fechaNacimiento = fechaNacimiento,
            genero = genero,
            estadoCivil = estadoCivil,
            numeroCelular = celular,
            correoElectronico = correo,
            password = clave
        )
        Log.d("FormViewModel", "Datos actualizados: ${loanRequest.value}")
    }

    fun updateLocationInfo(
        provincia: String,
        canton: String,
        distrito: String,
        direccionExacta: String
    ) {
        _loanRequest.value = _loanRequest.value?.copy(
            pais = "Costa Rica",
            provincia = provincia,
            canton = canton,
            distrito = distrito,
            direccionExacta = direccionExacta
        )
        Log.d("FormViewModel", "Datos actualizados: ${loanRequest.value}")
    }

    fun updateEconomicInfo(
        cuentaIBAN: String,
        moneda: String,
        salario: Int,
        actividadEconomica: String,
        profesion: String,
        fidelidad: String,
        ccss: String,
        referrer: String,
        referrerCode: String?,
        referrerName: String?
    ) {
        _loanRequest.value = _loanRequest.value?.copy(
            cuentaIBAN = cuentaIBAN,
            moneda = moneda,
            salarioMensual = salario,
            actividadEconomica = actividadEconomica,
            profesion = profesion,
            fidelidad = fidelidad,
            cuentaOrderPatronal = ccss,
            referido = referrer,
            referidoCedula = referrerCode ?: "",
            referidoNombre = referrerName ?: ""
        )
        Log.d("FormViewModel", "Datos actualizados: ${loanRequest.value}")
    }

    // Propuesta de préstamo
    private val _loanProposal = MutableLiveData<CreateLoanResponse?>()
    val loanProposal: LiveData<CreateLoanResponse?> get() = _loanProposal

    fun setLoanProposal(data: CreateLoanResponse) {
        _loanProposal.value = data
    }

    // Auth token (temp)
    private val _authToken = MutableLiveData<String?>()
    val authToken: LiveData<String?> get() = _authToken

    fun setAuthToken(data: String) {
        _authToken.value = data
    }
}