package com.rayo.rayoxml.cr.ui.loan

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentProposalConfirmationBinding
import com.rayo.rayoxml.cr.services.Loan.LoanRepository
import com.rayo.rayoxml.cr.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.cr.services.Loan.PLPLoanStep5Request
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.cr.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.cr.ui.renewal.RenewalFragment
import com.rayo.rayoxml.cr.viewModels.RenewalViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProposalConfirmationFragment(private val viewModel: UserViewModel) : Fragment() {

    private var _binding: FragmentProposalConfirmationBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: LoanRepository
    private lateinit var validationViewModel: LoanValidationStepViewModel
    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment
    private val SUCCESSFUL_FORM_RESULT: String = "La solicitud fue procesada exitosamente"

    var loanType = ""
    private var formId: String? = null

    private val renewalViewModel: RenewalViewModel by activityViewModels()

    /*private lateinit var repository: LoanRepository

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    // Modelo compartido

    private lateinit var loanRenewalInfoLoadResponse: LoanRenewalInfoLoadResponse
    private lateinit var loanRpRenewalInfoLoadResponse: LoanStepFivePlusLoadResponse
    private var formId: String = ""
    private var loanTerm: Int = 0
    private var loanType: String = ""
    private var technologyEnabled: Boolean = false

    private val SUCCESSFUL_FORM_RESULT: String = "La solicitud fue procesada exitosamente"*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProposalConfirmationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        repository = LoanRepository()

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        /*repository = LoanRepository()

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("ProposalConfirmationFragment", "Tipo de préstamo: ${loanType}")
            }
        }

        // Check tecnología
        renewalViewModel.technologyCheck.observe(viewLifecycleOwner) { option ->
            option?.let {
                technologyEnabled = it
                Log.d("ProposalConfirmationFragment", "Datos de check de tecnología: ${technologyEnabled}")
            }
        }

        // Response previo
        renewalViewModel.infoResponse.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanRenewalInfoLoadResponse = it
                Log.d("ProposalConfirmationFragment", "Datos de info de renovación (response): ${loanRenewalInfoLoadResponse}")
            }
        }
        // Response previo RP
        renewalViewModel.infoRpResponse.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanRpRenewalInfoLoadResponse = it
                Log.d("ProposalConfirmationFragment", "Datos de info de renovación RP (response): ${loanRpRenewalInfoLoadResponse}")
            }
        }*/

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("ProposalConfirmationFragment", "Tipo de préstamo: ${loanType}")
            }
        }

        // Formulario PLP
        renewalViewModel.formId.observe(viewLifecycleOwner) { data ->
            data?.let {
                formId = it
                Log.d("PaymentMethodScreenFragment", "Datos de formulario PLP: ${formId}")
            }
        }

        val buttonFiltro1 = view.findViewById<MaterialButton>(R.id.buttonTerms)
        val optionsFiltro1 = view.findViewById<LinearLayout>(R.id.optionsTerms)
        optionsFiltro1.visibility = View.GONE  // Ocultar

        val buttonFiltro2 = view.findViewById<MaterialButton>(R.id.button_acceptance)
        val optionsFiltro2 = view.findViewById<LinearLayout>(R.id.options_acceptance)
        optionsFiltro2.visibility = View.GONE  // Ocultar

        binding.checkboxUserTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnConfirmSend.isEnabled = isChecked
        }

        binding.btnConfirmSend.setOnClickListener {
            when (parentFragment) {
                is FormFragment -> {
                    Log.d("ProposalConfirmationFragment", "Ingresando a crear préstamo")
                    (parentFragment as FormFragment).goToNextStep()
                }
                is RenewalFragment ->{
                    if(loanType == "PLP"){
                        Log.d("ProposalConfirmationFragment", "Ingresando a renovar préstamo PLP")
                        lifecycleScope.launch {
                            submitLoanPLP()
                        }
                        //(parentFragment as? RenewalFragment)?.goToNextStep()
                    }else{
                        Log.d("ProposalConfirmationFragment", "Ingresando a renovar préstamo")
                        (parentFragment as? RenewalFragment)?.goToNextStep()
                    }
                }
            }
        }

        // Color checks
        val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
        val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

        // Mostrar/Ocultar opciones dentro del contenedor con borde
        buttonFiltro1.setOnClickListener {

            optionsFiltro1.visibility = if (optionsFiltro1.visibility == View.VISIBLE) View.GONE else View.VISIBLE

        }

        buttonFiltro2.setOnClickListener {

            optionsFiltro2.visibility = if (optionsFiltro2.visibility == View.VISIBLE) View.GONE else View.VISIBLE

        }

        binding.checkboxUserTerms.setOnClickListener {
            val isChecked = binding.checkboxUserTerms.isChecked
            binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
        }
    }

    private suspend fun submitLoanPLP(){

        val formData = PLPLoanStep5Request(
            formulario = formId.toString(),
            plazoSeleccionado = 15
        )

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val plpLoanStep5Response = withContext(Dispatchers.IO) {
                repository.getDataPlpStep5(formData)
            }

            Log.d("ProposalConfirmationFragment", "PLP loan step 5 Data: ${plpLoanStep5Response}")

            if (plpLoanStep5Response != null) {
                if (plpLoanStep5Response.solicitud.codigo == "200") {

                    // Actualizar request y response en viewModel
                    //renewalViewModel.setProposalSubmitRequestData(formData)
                    //renewalViewModel.setProposalSubmitResponseData(plpLoanStep4Response)

                    Log.d("ProposalConfirmationFragment", "Solicitud paso 5 OK")

                    // Continuar
                    // Renovar préstamo
                    if (plpLoanStep5Response.solicitud.result == SUCCESSFUL_FORM_RESULT) {
                        // Mostrar pantalla de confirmación
                        Log.d("ProposalConfirmationFragment", "Préstamo PLP renovado: ${plpLoanStep5Response.solicitud.formulario}")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.SUCCESS_BANK)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }else{
                        // Mostrar pantalla de error
                        Log.d("ProposalConfirmationFragment", "Error")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }

                } else {
                    Log.d("ProposalConfirmationFragment", "plpLoanStep5Response nulo")
                    val bundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                }

                loadingDialog.dismiss() // ocultar loading
            }else {
                Log.d("ProposalConfirmationFragment", "plpLoanStep5Response nulo")
            }

        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("ProposalConfirmationFragment", "Error obteniendo datos", e)
        }
    }
}