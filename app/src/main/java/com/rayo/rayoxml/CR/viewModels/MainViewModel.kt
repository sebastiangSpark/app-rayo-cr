package com.rayo.rayoxml.cr.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rayo.rayoxml.R

class MainViewModel : ViewModel() {

    private val _navigationEvent = MutableLiveData<Int?>()
    val navigationEvent: LiveData<Int?> = _navigationEvent

    fun navigateTo(destinationId: Int) {
        _navigationEvent.value = destinationId
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    private val _toolbarImageVisibility = MutableLiveData<Boolean>()
    val toolbarImageVisibility: LiveData<Boolean> = _toolbarImageVisibility

    fun updateToolbarImageVisibility(step: Int) {
        val isVisible = step != 1 // Oculta la imagen en el step 1, muéstrala en los demás
        _toolbarImageVisibility.value = isVisible
    }

    private val _toolbarVisibility = MutableLiveData<Map<String, Boolean>>()
    val toolbarVisibility: LiveData<Map<String, Boolean>> = _toolbarVisibility

    fun updateToolbarVisibility(destinationId: Int) {
        val visibilityMap = when (destinationId) {
            R.id.loadingFragment, R.id.viewPagerFragment, R.id.loginFragment, R.id.loanOutcomeFragment, R.id.addressInfoFormFragment,
            R.id.recoverPasswordFragment,R.id.disbursementApprovalFragment, R.id.selectCountryFragment, R.id.UserVerifyFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.loanFragment -> mapOf(
                "toolbarMenu" to true,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.loanTypesFragment,R.id.miniScreenFragment,R.id.plusScreenFragment,R.id.largeScreenFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to false,
                "toolbarLoanTypes" to true
            )
            R.id.tumiDisbursementFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to true,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.bankDisbursementFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to true,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.formFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDesembolso" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to true,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.renewalFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to true,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.paymentMethodScreenFragment-> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to true,
                "bottomNav" to false,
                "toolbarLoanTypes" to false
            )
            R.id.personalLoanFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to true,
                "toolbarLoanTypes" to false
            )
            R.id.loanDetailFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarPersonalLoan" to false,
                "toolbarLayout" to false,
                "bottomNav" to true,
                "toolbarLoanTypes" to false
            )
            R.id.profileFragment -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to false,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to true,
                "toolbarLayoutAddress" to false,
                "toolbarLayout" to false,
                "bottomNav" to true,
                "toolbarLoanTypes" to false
            )
            else -> mapOf(
                "toolbarMenu" to false,
                "toolbarProfile" to true,
                "toolbarDisbursement" to false,
                "toolbarPersonalProfile" to false,
                "toolbarLayoutAddress" to false,
                "toolbarPersonalLoan" to false,
                "toolbarLayout" to false,
                "bottomNav" to true,
                "toolbarLoanTypes" to false
            )
        }
        _toolbarVisibility.value = visibilityMap
    }

    private val _drawerState = MutableLiveData<Boolean>()
    val drawerState: LiveData<Boolean> = _drawerState

    fun toggleDrawer() {
        _drawerState.value = _drawerState.value != true
    }

    fun onLogoutClicked() {
        _navigationEvent.value = R.id.loginFragment
        _drawerState.value = false
    }


    private val _isMenuItemVisible = MutableLiveData<Boolean>()
    val isMenuItemVisible: LiveData<Boolean> = _isMenuItemVisible

    fun updateMenuItemVisibility(destinationId: Int) {
        val isVisible = destinationId == R.id.homeFragment
        _isMenuItemVisible.value = isVisible
        Log.d("MainViewModel", "MenuItem visible: $isVisible (destinationId: $destinationId)")
    }

}