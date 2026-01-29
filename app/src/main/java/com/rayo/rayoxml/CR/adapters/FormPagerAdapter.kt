package com.rayo.rayoxml.cr.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.ui.loan.AddressInfoFormFragment
import com.rayo.rayoxml.cr.ui.loan.BankInfoFormFragment
import com.rayo.rayoxml.cr.ui.loan.FormFragment
import com.rayo.rayoxml.cr.ui.loan.LoanProposalFragment
import com.rayo.rayoxml.cr.ui.loan.PersonalInfoFormFragment

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
            1 -> AddressInfoFormFragment()
            2 -> BankInfoFormFragment(viewModel)
            3 -> LoanProposalFragment(viewModel)
            /*4 -> ProposalConfirmationFragment(viewModel)*/
            else -> throw IllegalArgumentException("Paso desconocido")
        }
    }
}
