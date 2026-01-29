package com.rayo.rayoxml.mx.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.ui.loan.BankInfoFormFragment
import com.rayo.rayoxml.mx.ui.loan.FormFragment
import com.rayo.rayoxml.mx.ui.loan.LoanProposalFragment
import com.rayo.rayoxml.mx.ui.loan.PaymentMethodScreenFragment
import com.rayo.rayoxml.mx.ui.loan.PersonalInfoFormFragment
import com.rayo.rayoxml.mx.ui.loan.ProposalConfirmationFragment

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
            0 -> PersonalInfoFormFragment(viewModel)
            1 -> {
                val fragment = BankInfoFormFragment(viewModel)
                fragment.arguments = bundle // <-- Enviamos los datos
                fragment
            }
            2 -> LoanProposalFragment(viewModel)
            3 -> PaymentMethodScreenFragment()
            4 -> ProposalConfirmationFragment(viewModel)

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
            else -> throw IllegalArgumentException("Paso desconocido")
        }
    }
}
