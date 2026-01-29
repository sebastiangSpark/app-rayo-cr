package com.rayo.rayoxml.co.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.R
import androidx.fragment.app.activityViewModels
import com.rayo.rayoxml.databinding.FragmentBankAccountDialogBinding
import com.rayo.rayoxml.co.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.co.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.co.services.User.Solicitud
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager

class BankDialogFragment : DialogFragment() {

    private var _binding: FragmentBankAccountDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog)
    }

    private var userData: Solicitud? = null
    private var personalData: LoanStepOneRequest? = null
    private var bankData: LoanStepTwoRequest? = null

    // viewmodel de datos de usuario
    private lateinit var loanViewModel: LoanStepOneViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankAccountDialogBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aprobar
        binding.btnSubmitBank.setOnClickListener {
            parentFragmentManager.setFragmentResult("bankDialogResult", bundleOf("result" to "approved"))
            dismiss()
        }

        // Cancelar
        binding.btnBackBank.setOnClickListener {
            Log.d("BankDialogFragment", "Cancelando desembolso")
            parentFragmentManager.setFragmentResult("bankDialogResult", bundleOf("result" to "cancelled"))
            dismiss()
        }

        // Asignar viewModel
        val userPreferencesManager = PreferencesManager(requireContext())

        val viewModel: UserViewModel by activityViewModels {
            UserViewModelFactory(
                UserRepository(),
                preferencesManager = userPreferencesManager
            )
        }

        viewModel.bankdData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                bankData = data
                // Cargar datos banco
                if(bankData != null){
                    if(bankData!!.banco.isNotEmpty()) binding.tvBankName.setText(bankData!!.banco)
                    if(bankData!!.referenciaBancaria.isNotEmpty()) binding.tvAccountNumber.setText(bankData!!.referenciaBancaria)
                }
            }
        }

        // Banco y cuenta en renovación
        viewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("BankDialogFragment", "Datos user: ${data}")
                userData = data

                if(userData!!.banco!!.isNotEmpty()) binding.tvBankName.setText(userData!!.banco)
                if(userData!!.numeroCuenta!!.isNotEmpty()) binding.tvAccountNumber.setText(userData!!.numeroCuenta)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}