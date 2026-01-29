package com.rayo.rayoxml.cr.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.ui.loan.PaymentMethodScreenFragment
import com.rayo.rayoxml.cr.ui.loan.ProposalConfirmationFragment
import com.rayo.rayoxml.cr.ui.renewal.RenewalPlpStep1Fragment
import com.rayo.rayoxml.cr.ui.renewal.RenewalPlpStep2Fragment
import com.rayo.rayoxml.cr.ui.renewal.RenewalPlpStep3Fragment
import com.rayo.rayoxml.cr.ui.renewal.RenewalProposalFragment
import com.rayo.rayoxml.cr.viewModels.RenewalViewModel

class RenewalPagerAdapter(fragment: Fragment, private val viewModel: UserViewModel, private val renewalViewModel: RenewalViewModel, private val loanType: String = "") : FragmentStateAdapter(fragment)  {

    override fun getItemCount(): Int = 6

    /*override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RenewalProposalFragment()
            1 -> PaymentMethodScreenFragment()
            2 -> ProposalConfirmationFragment(viewModel)
            else -> throw IllegalArgumentException("Paso desconocido")
        }
    }*/

    //var isFirstStep: Boolean? = true

    override fun createFragment(position: Int): Fragment {
        if( loanType == "PLP") Log.d("RenewalFragment", "Cargando flujo PLP")
        else Log.d("RenewalFragment", "Cargando flujo RP/Mini")

        return when {
            loanType == "PLP" -> when (position) {
                0 -> RenewalPlpStep1Fragment()
                1 -> RenewalPlpStep2Fragment()
                2 -> RenewalPlpStep3Fragment()
                3 -> RenewalProposalFragment()
                4 -> PaymentMethodScreenFragment()
                5 -> ProposalConfirmationFragment(viewModel)
                else -> throw IllegalArgumentException("Paso desconocido")

            }
            else -> when (position) {
                0 -> RenewalProposalFragment()
                //1 -> PaymentMethodScreenFragment()
                //2 -> ProposalConfirmationFragment(viewModel)
                else -> throw IllegalArgumentException("Paso desconocido")
            }
        }
    }
}