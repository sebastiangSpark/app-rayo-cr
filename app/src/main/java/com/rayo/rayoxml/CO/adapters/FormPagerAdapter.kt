package com.rayo.rayoxml.co.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.ui.loan.BankInfoFormFragment
import com.rayo.rayoxml.co.ui.loan.FormFragment
import com.rayo.rayoxml.co.ui.loan.LoanProposalFragment
import com.rayo.rayoxml.co.ui.loan.PaymentMethodScreenFragment
import com.rayo.rayoxml.co.ui.loan.PersonalInfoFormFragment
import com.rayo.rayoxml.co.ui.loan.ProposalConfirmationFragment

interface StepFragment {
    fun getStepTitle(): String
}

class FormPagerAdapter(activity: FormFragment, private val viewModel: UserViewModel, private val bundle: Bundle? = null) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5 // Número de pasos

    override fun getItemId(position: Int): Long {
        return position.toLong() // Asigna un ID único a cada fragmento
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemId in 0..4 // Asegura que el ViewPager2 reconoce los cambios
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PersonalInfoFormFragment(viewModel).apply {
                arguments = bundle
            }
            1 -> BankInfoFormFragment(viewModel).apply {
                arguments = bundle
            }
            2 -> LoanProposalFragment(viewModel).apply {
                arguments = bundle
            }
            3 -> PaymentMethodScreenFragment().apply {
                arguments = bundle
            }
            4 -> ProposalConfirmationFragment(viewModel).apply {
                arguments = bundle
            }
            else -> throw IllegalArgumentException("Paso desconocido")
        }
    }

    fun getTitleForPosition(position: Int): String {
        return when (position) {
            0 -> "Información personal"
            1 -> "Información Bancaria"
            2 -> "Propuesta del Préstamo"
            3, 4 -> "Confirmación de la Propuesta"
            else -> "Formulario"
        }
    }

            /*1 -> {
                val fragment = BankInfoFormFragment()
                fragment.arguments = bundle // <-- Enviamos los datos
                fragment
            }
<<<<<<< HEAD
            2 -> LoanProposalFragment()
            3 -> PaymentMethodScreenFragment()
            4 -> ProposalConfirmationFragment()
=======
            2 -> {
                val fragment = LoanProposalFragment()
                fragment.arguments = bundle
                fragment
            }
            3 -> {
                val fragment = PaymentMethodScreenFragment()
                fragment.arguments = bundle
                fragment
            }
            4 -> {
                val fragment = ProposalConfirmationFragment()
                fragment.arguments = bundle
                fragment
            }*/
            /*5 -> BankDisbursementFragment()
            6 -> TumiDisbursementFragment()
            7 -> DisbursementApprovalFragment()*/

}
