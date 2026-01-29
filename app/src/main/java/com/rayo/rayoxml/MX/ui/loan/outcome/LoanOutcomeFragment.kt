package com.rayo.rayoxml.mx.ui.loan.outcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.viewModels.AuthViewModel
import com.rayo.rayoxml.mx.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.databinding.FragmentLoanOutcomeBinding
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LoanOutcomeFragment : Fragment() {

    private var _binding: FragmentLoanOutcomeBinding? = null
    private val binding get() = _binding!!
    private var outcome: LoanOutcome? = null

    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanOutcomeBinding.inflate(inflater, container, false)
        outcome = arguments?.getSerializable("outcome") as? LoanOutcome ?: LoanOutcome.SUCCESS_BANK

        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        preferencesManager = PreferencesManager(requireContext())

        val factory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnGoHome.setOnClickListener{

            findNavController().popBackStack(R.id.homeFragment, true) // Elimina `homeFragment` de la pila

            // Redireccionar a pantalla siguiente
            viewLifecycleOwner.lifecycleScope.launch {
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

            /*findNavController().popBackStack(R.id.homeFragment, true) // Elimina `homeFragment` de la pila
            // Redireccionar a pantalla siguiente
            viewLifecycleOwner.lifecycleScope.launch {
                val preferencesManager = PreferencesManager(requireContext())
                preferencesManager.isLoggedIn.collect { isLogged ->
                    if (isLogged) {
                        findNavController().navigate(R.id.homeFragment)
                    }else{
                        findNavController().navigate(R.id.loginFragment)
                    }
                }
            }*/
        }
    }

    private fun setupUI() {
        val layoutId = when (outcome) {
            LoanOutcome.REJECTED -> R.layout.rejected
            LoanOutcome.SUCCESS_BANK -> R.layout.success_bank
            LoanOutcome.SUCCESS_TUMI -> R.layout.success_tumi
            else -> null
        }
        // Inflate correct layout inside the container
        layoutId?.let {
            val layoutInflater = LayoutInflater.from(requireContext())
            val view = layoutInflater.inflate(it, binding.cardContainerOutcome, false)
            binding.cardContainerOutcome.removeAllViews() // Remove previous views if any
            binding.cardContainerOutcome.addView(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}