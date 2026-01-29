package com.rayo.rayoxml.co.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FormViewModel : ViewModel() {

    // Datos del primer paso (PersonalInfoFormFragment)
    var firstName: String = ""
    var lastName: String = ""
    var document: String = ""
    var dateOfBirth: String = ""
    var phone: String = ""
    var email: String = ""
    var isFormalEmployee: String = ""
    var hasAccount: String = ""

    // Datos del formulario del banco
    val departamento = MutableLiveData<String>()
    val ciudadDepartamento = MutableLiveData<String>()
    val direccionExacta = MutableLiveData<String>()
    val banco = MutableLiveData<String>()
    val tipoCuenta = MutableLiveData<String>()
    val referenciaBancaria = MutableLiveData<String>()
    val referenciaBancaria2 = MutableLiveData<String>()

    // Datos del formulario de dirección
    val via = MutableLiveData<String>()
    val number = MutableLiveData<String>()
    val carrer = MutableLiveData<String>()
    val complement = MutableLiveData<String>()
    val detail = MutableLiveData<String>()
}