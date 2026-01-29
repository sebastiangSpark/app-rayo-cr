package com.rayo.rayoxml.mx.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalInfoLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalInfoLoadResponse
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalLoadResponse
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalSubmitResponse
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveLoadResponse
import com.rayo.rayoxml.mx.services.Loan.LoanStepFivePlusLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFivePlusLoadResponse
import com.rayo.rayoxml.mx.services.Loan.PLPLoanStep2Request
import com.rayo.rayoxml.mx.services.Loan.PLPLoanStep3Request

class RenewalViewModel : ViewModel() {

    // Formulario
    private val _formId = MutableLiveData<String?>()
    val formId: LiveData<String?> get() = _formId

    fun setFormId(id: String) {
        _formId.value = id
    }

    // Technology check
    private val _technologyCheck = MutableLiveData<Boolean?>()
    val technologyCheck: LiveData<Boolean?> get() = _technologyCheck

    fun setTechnologyCheck(check: Boolean) {
        _technologyCheck.value = check
    }

    // Cuotas
    private val _loanTerm = MutableLiveData<Int?>()
    val loanTerm: LiveData<Int?> get() = _loanTerm

    fun setLoanTerm(term: Int) {
        _loanTerm.value = term
    }

    // Descuento administrativo
    private val _adminDiscount = MutableLiveData<String?>()
    val adminDiscount: LiveData<String?> get() = _adminDiscount

    fun setAdminDiscount(discount: String) {
        _adminDiscount.value = discount
    }

    // Descuento 10
    private val _tenDiscount = MutableLiveData<String?>()
    val tenDiscount: LiveData<String?> get() = _tenDiscount

    fun setTenDiscount(discount: String) {
        _tenDiscount.value = discount
    }

    // Descuento +4 prestamo
    private val _discountRate = MutableLiveData<String?>()
    val discountRate: LiveData<String?> get() = _discountRate

    fun setDiscountRate(discount: String) {
        _discountRate.value = discount
    }

    // Préstamo seleccionado
    private val _loanType = MutableLiveData<String?>()
    val loanType: LiveData<String?> get() = _loanType

    fun setLoanType(data: String) {
        _loanType.value = data
    }

    // Desembolso auto tumipay
    private val _tumipayDisbursement = MutableLiveData<String?>()
    val tumipayDisbursement: LiveData<String?> get() = _tumipayDisbursement

    fun setTumipayDisbursement(data: String) {
        _tumipayDisbursement.value = data
    }


    // Proposal Load
    private val _proposalLoadRequest = MutableLiveData<LoanRenewalProposalLoadRequest?>()
    val proposalLoadRequest: LiveData<LoanRenewalProposalLoadRequest?> get() = _proposalLoadRequest

    fun setProposalLoadRequestData(data: LoanRenewalProposalLoadRequest) {
        _proposalLoadRequest.value = data
    }

    private val _proposalLoadResponse = MutableLiveData<LoanRenewalProposalLoadResponse?>()
    val proposalLoadResponse: LiveData<LoanRenewalProposalLoadResponse?> get() = _proposalLoadResponse

    fun setProposalLoadResponseData(data: LoanRenewalProposalLoadResponse) {
        _proposalLoadResponse.value = data
    }

    // Proposal submit
    private val _proposalSubmitRequest = MutableLiveData<LoanRenewalProposalSubmitRequest?>()
    val proposalSubmitRequest: LiveData<LoanRenewalProposalSubmitRequest?> get() = _proposalSubmitRequest

    fun setProposalSubmitRequestData(data: LoanRenewalProposalSubmitRequest) {
        _proposalSubmitRequest.value = data
    }

    private val _proposalSubmitResponse = MutableLiveData<LoanRenewalProposalSubmitResponse?>()
    val proposalSubmitResponse: LiveData<LoanRenewalProposalSubmitResponse?> get() = _proposalSubmitResponse

    fun setProposalSubmitResponseData(data: LoanRenewalProposalSubmitResponse) {
        _proposalSubmitResponse.value = data
    }

    // Form
    private val _formRequest = MutableLiveData<LoanStepFiveLoadRequest?>()
    val formRequest: LiveData<LoanStepFiveLoadRequest?> get() = _formRequest

    fun setFormRequest(data: LoanStepFiveLoadRequest) {
        _formRequest.value = data
    }

    private val _formResponse = MutableLiveData<LoanStepFiveLoadResponse?>()
    val formResponse: LiveData<LoanStepFiveLoadResponse?> get() = _formResponse

    fun setFormResponseData(data: LoanStepFiveLoadResponse) {
        _formResponse.value = data
    }

    // Information
    private val _infoRequest = MutableLiveData<LoanRenewalInfoLoadRequest?>()
    val infoRequest: LiveData<LoanRenewalInfoLoadRequest?> get() = _infoRequest

    fun setInfoRequestData(data: LoanRenewalInfoLoadRequest) {
        _infoRequest.value = data
    }

    private val _infoResponse = MutableLiveData<LoanRenewalInfoLoadResponse?>()
    val infoResponse: LiveData<LoanRenewalInfoLoadResponse?> get() = _infoResponse

    fun setInfoResponseData(data: LoanRenewalInfoLoadResponse) {
        _infoResponse.value = data
    }

    // Information RP
    private val _infoRpRequest = MutableLiveData<LoanStepFivePlusLoadRequest?>()
    val infoRpRequest: LiveData<LoanStepFivePlusLoadRequest?> get() = _infoRpRequest

    fun setInfoRpRequestData(data: LoanStepFivePlusLoadRequest) {
        _infoRpRequest.value = data
    }

    private val _infoRpResponse = MutableLiveData<LoanStepFivePlusLoadResponse?>()
    val infoRpResponse: LiveData<LoanStepFivePlusLoadResponse?> get() = _infoRpResponse

    fun setInfoRpResponseData(data: LoanStepFivePlusLoadResponse) {
        _infoRpResponse.value = data
    }

    // Formulario PLP paso 2
    private val _plpStep2Request = MutableLiveData<PLPLoanStep2Request?>()
    val plpStep2Request: LiveData<PLPLoanStep2Request?> get() = _plpStep2Request

    fun setPlpStep2Request(data: PLPLoanStep2Request) {
        _plpStep2Request.value = data
    }

    // Formulario PLP paso 3
    private val _plpStep3Request = MutableLiveData<PLPLoanStep3Request?>()
    val plpStep3Request: LiveData<PLPLoanStep3Request?> get() = _plpStep3Request

    fun setPlpStep3Request(data: PLPLoanStep3Request) {
        _plpStep3Request.value = data
    }
}