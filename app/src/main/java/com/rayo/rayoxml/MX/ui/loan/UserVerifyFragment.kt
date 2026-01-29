package com.rayo.rayoxml.mx.ui.loan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentUserVerifyBinding
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModelFactory
import com.rayo.rayoxml.mx.services.Loan.OTPVerifyRequest
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserVerifyFragment : Fragment() {

    private var formId: String? = ""
    private lateinit var loanViewModel: LoanStepOneViewModel

    private var _binding: FragmentUserVerifyBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanStepOneViewModelFactory(repository, preferencesManager)
        loanViewModel = ViewModelProvider(requireActivity(), factory)[LoanStepOneViewModel::class.java]

        loanViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                formId = user.formulario
                Log.d("UserVerifyFragment", "ID form ${formId}")
            }
        }

        binding.btnConfirmVerification.setOnClickListener {

            val userCode = "${binding.codeDigitCero}${binding.codeDigitOne}${binding.codeDigitTwo}${binding.codeDigitThree}${binding.codeDigitFour}${binding.codeDigitFive}"
            Log.d("UserVerifyFragment", "Código: $userCode")

            val request = OTPVerifyRequest(
                formulario = formId!!,
                codigoOTP = userCode
            )
            lifecycleScope.launch {
                val OtpResponse = withContext(Dispatchers.IO) {
                    repository.getDataOTPVerify(request)
                }

                Log.d("UserVerifyFragment", "OTP response: ${OtpResponse}")

                if(OtpResponse != null && OtpResponse.solicitud.codigo == "200"){
                    findNavController().navigate(
                        R.id.action_UserVerifyFragment_to_formFragment,
                        bundleOf("goToStep3" to true) // Enviamos un argument
                    )
                }else{
                    Log.d("UserVerifyFragment", "OTP response in correcta")
                }
            }

        }
    }
}