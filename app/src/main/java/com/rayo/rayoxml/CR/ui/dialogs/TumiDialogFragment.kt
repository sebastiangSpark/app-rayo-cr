package com.rayo.rayoxml.cr.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.R
import androidx.fragment.app.activityViewModels
import com.rayo.rayoxml.databinding.FragmentTumiDialogBinding
import com.rayo.rayoxml.cr.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.cr.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.cr.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.utils.PreferencesManager

class TumiDialogFragment : DialogFragment() {

    private var _binding: FragmentTumiDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog)
    }

    private var personalData: LoanStepOneRequest? = null
    private var bankData: LoanStepTwoRequest? = null

    // viewmodel de datos de usuario
    private lateinit var loanViewModel: LoanStepOneViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTumiDialogBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asignar viewModel
        val userPreferencesManager = PreferencesManager(requireContext())

        val viewModel: UserViewModel by activityViewModels {
            UserViewModelFactory(
                UserRepository(),
                preferencesManager = userPreferencesManager
            )
        }

        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                personalData = data
                // Cargar número tumipay
                if(personalData != null){
                    if(personalData!!.celular.isNotEmpty()) binding.textDialogTumiNumberAccount.setText(personalData!!.celular)
                }
            }

        }

        binding.btnSubmitTumi.setOnClickListener {
            parentFragmentManager.setFragmentResult("tumiDialogResult", bundleOf("result" to "approved"))
            dismiss()
        }

        // Cancelar
        binding.btnBackTumi.setOnClickListener {
            parentFragmentManager.setFragmentResult("tumiDialogResult", bundleOf("result" to "cancelled"))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}