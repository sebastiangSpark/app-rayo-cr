package com.rayo.rayoxml.cr.ui.disbursement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.viewModels.AuthViewModel
import com.rayo.rayoxml.cr.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.databinding.FragmentDisbursementApprovalBinding
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DisbursementApprovalFragment : Fragment() {

    private var _binding: FragmentDisbursementApprovalBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisbursementApprovalBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        val rejection = arguments?.getBoolean("shouldReject") ?: false
        val factory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        showBankCard(rejection)

        binding.btnfinishDisbursement.setOnClickListener {
            // Redireccionar a pantalla siguiente
            viewLifecycleOwner.lifecycleScope.launch {
                //val preferencesManager = PreferencesManager(requireContext())
                /*val isLogged = authViewModel.isLoggedIn.first() // Get first emitted value
                if (isLogged) {
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    findNavController().navigate(R.id.loginFragment)
                }*/
                val isLoggedIn = authViewModel.isLoggedIn.firstOrNull() ?: false

                val destination = if (isLoggedIn) {
                    R.id.homeFragment
                } else {
                    R.id.loginFragment
                }

                if (findNavController().currentDestination?.id != destination) {
                    findNavController().navigate(destination)
                }
            }
        }
    }

    private fun showBankCard(rejection: Boolean) {
        var bankCardView: View;

        if (rejection) {
            bankCardView = layoutInflater.inflate(
                R.layout.cr_card_reject_disbursement,
                binding.cardContainerDisbursement,
                false
            )
            val linkRejection = bankCardView.findViewById<View>(R.id.tvRejectRedirectPage)
            linkRejection.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://rayo.cr/")
                startActivity(intent)
            }
        } else {
            bankCardView = layoutInflater.inflate(
                R.layout.cr_card_approval_disbursement,
                binding.cardContainerDisbursement,
                false
            )
        }
        // Limpiar el contenedor y agregar la nueva vista
        binding.cardContainerDisbursement.removeAllViews()
        binding.cardContainerDisbursement.addView(bankCardView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}