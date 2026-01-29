package com.rayo.rayoxml.co.ui.disbursement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentTumiDisbursementBinding
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.LoanStepFiveLoadRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepFiveSubmitRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.co.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.co.services.Loan.LoanTumiPayRequest
import com.rayo.rayoxml.co.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.co.services.Loan.LoanValidationStepViewModelFactory
import com.rayo.rayoxml.co.services.Loan.SolicitudStepFiveLoad
import com.rayo.rayoxml.co.services.User.Solicitud
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.ui.dialogs.TumiDialogFragment
import com.rayo.rayoxml.co.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.co.viewModels.RenewalViewModel
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TumiDisbursementFragment() : Fragment() {

    private lateinit var viewModel: UserViewModel

    private var _binding: FragmentTumiDisbursementBinding? = null
    private val binding get() = _binding!!

    private var formId: String? = null
    private var loanTerm: Int = 0
    private var loanType: String = ""
    private lateinit var repository: LoanRepository

    private val SUCCESSFUL_FORM_RESULT: String = "La solicitud fue procesada exitosamente"
    private val SUCCESSFUL_DISBURSEMENT_RESULT: String = "Ok"

    private var userData: Solicitud? = null
    private var personalData: LoanStepOneRequest? = null
    private var bankData: LoanStepTwoRequest? = null

    // viewmodel de datos de usuario
    private lateinit var loanViewModel: LoanStepOneViewModel
    private lateinit var validationViewModel: LoanValidationStepViewModel

    private val renewalViewModel: RenewalViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Asignar viewModel
        /*val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]*/

        _binding = FragmentTumiDisbursementBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asignar viewModel
        val userRepository = UserRepository()
        val userPreferencesManager = PreferencesManager(requireContext())
        val userFactory = UserViewModelFactory(userRepository, userPreferencesManager)
        //viewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]

        val viewModel: UserViewModel by activityViewModels {
            UserViewModelFactory(
                UserRepository(),
                preferencesManager = userPreferencesManager
            )
        }

        binding.buttonTumiDisbursement.setOnClickListener {
            showCustomDialog()
        }

        // Mostrar opción alternativa de pago
        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.bankDisbursementFragment)
        }

        // botón atrás
        binding.backToolbarIcon.setOnClickListener {
            goBack()
        }

        viewModel.formId.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("TumiDibursementFragment", "Formulario asignado user: $data")
                formId = data
            }
        }
        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            data?.let {
                Log.d("TumiDibursementFragment", "Datos personales user: $it")
                // Cargar datos banco
                if(it.celular.isNotEmpty()) binding.userBank.text = it.celular
            }
        }
        viewModel.bankdData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("TumiDibursementFragment", "Datos bancarios user: $data")
                bankData = data
            }
        }

        // Banco y cuenta en renovación
        viewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("TumiDibursementFragment", "Datos user: ${data}")
                userData = data
                if(userData!!.celular!!.isNotEmpty()) binding.userBank.text = userData!!.celular
            }
        }

        // Renovación
        renewalViewModel.formId.observe(viewLifecycleOwner) { data ->
            data?.let {
                formId = it
                Log.d("TumiDibursementFragment", "Formulario: ${formId}")
            }
        }
        renewalViewModel.loanTerm.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanTerm = it
                Log.d("TumiDibursementFragment", "Datos de plazo: ${loanTerm}")
            }
        }
        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("TumiDibursementFragment", "Tipo de préstamo: ${loanType}")
            }
        }

        // Obtener ViewModel compartido con la actividad
        repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanValidationStepViewModelFactory(repository, preferencesManager)
        validationViewModel = ViewModelProvider(requireActivity(), factory)[LoanValidationStepViewModel::class.java]

        validationViewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.codigo == "200") {
                formId = data.formulario
                Log.d("TumiDibursementFragment", "Formulario asignado validation: $data")
            }
        }

        // Listener de modal de aprobación desembolso
        parentFragmentManager.setFragmentResultListener("bankDialogResult", viewLifecycleOwner) { _, bundle ->
            val result = bundle.getString("result")
            if (result == "approved") {
                Log.d("TumiDibursementFragment", "Aprobado desembolso")
                // Continuar si se aprobó en modal
                viewLifecycleOwner.lifecycleScope.launch {
                    runValidations()
                }
            } else if (result == "cancelled") {
                Log.d("TumiDibursementFragment", "Cancelado desembolso")
            }
        }

        // Listener de modal de aprobación desembolso
        parentFragmentManager.setFragmentResultListener("tumiDialogResult", viewLifecycleOwner) { _, bundle ->
            val result = bundle.getString("result")
            if (result == "approved") {
                Log.d("Fragment", "Aprobado desembolso tumipay")
                // Continuar si se aprobó en modal
                viewLifecycleOwner.lifecycleScope.launch {
                    runValidations()
                }
            } else if (result == "cancelled") {
                Log.d("Fragment", "Cancelado desembolso tumipay")
            }
        }

    }

    private fun showCustomDialog() {
        val dialogFragment = TumiDialogFragment()
        dialogFragment.show(parentFragmentManager, "CustomDialogFragment")
    }

    private fun goBack() {
        if(loanTerm == 0){
            Log.d("TumiDibursementFragment", "Ir atrás crear préstamo")
            val bundle = bundleOf("step" to 4)
            findNavController().navigate(R.id.formFragment, bundle)
        }else{
            Log.d("TumiDibursementFragment", "Ir atrás renovación préstamo $loanType")
            val bundle: Bundle = if(loanType == "PLP") bundleOf("step" to 5)
            else bundleOf("step" to 2)
            findNavController().navigate(R.id.renewalFragment, bundle)
        }
    }

    private suspend fun runValidations(){
        if(loanTerm == 0){
            loanTerm = bankData!!.plazoSeleccionado
        }
        Log.d("TumiDibursementFragment", "Formulario paso tumipay: $formId")
        Log.d("TumiDibursementFragment", "Plazo paso tumipay: ${loanTerm}")
        if(formId != null){
            val formData = LoanStepFiveLoadRequest(formId!!)

            val loanStepFiveLoadResponse = withContext(Dispatchers.IO) {
                repository.getDataStepFiveLoad(formData)
            }

            Log.d("TumiDibursementFragment", "Loan step five: ${loanStepFiveLoadResponse}")

            if (loanStepFiveLoadResponse != null){
                if(loanStepFiveLoadResponse.solicitud.codigo == "200"){

                    val data: SolicitudStepFiveLoad = loanStepFiveLoadResponse.solicitud
                    Log.d("TumiDibursementFragment", "Formulario paso tumipay: ${data.formulario}")

                    if(data.formulario.isNotEmpty()){
                        // STEP 5
                        if(loanTerm != null && data != null){
                            val formDataStep5 = LoanStepFiveSubmitRequest(
                                formulario = data.formulario,
                                plazoSeleccionado = loanTerm
                            )
                            val loanStepFiveSubmitResponse = withContext(Dispatchers.IO) {
                                repository.getDataStepFiveSubmit(formDataStep5)
                            }
                            if (loanStepFiveSubmitResponse != null) {
                                if(loanStepFiveSubmitResponse.solicitud.codigo == "200" && loanStepFiveSubmitResponse.solicitud.result == SUCCESSFUL_FORM_RESULT){
                                    Log.d("TumiDibursementFragment", "Préstamo creado: ${loanStepFiveSubmitResponse.solicitud.prestamo}")

                                    // Desembolso TumiPay
                                    if(loanStepFiveSubmitResponse.solicitud.prestamo!!.isNotEmpty()){
                                        tumiPayDisbursement(loanStepFiveSubmitResponse.solicitud.prestamo)
                                    }

                                }else{
                                    // Mostrar pantalla de error
                                    Log.d("TumiDibursementFragment", "Error")
                                    val bundle = Bundle().apply {
                                        putSerializable("outcome", LoanOutcome.REJECTED)
                                    }
                                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                                }
                            }
                        }else{
                            Log.d("TumiDibursementFragment", "User data null")
                            val bundle = Bundle().apply {
                                putSerializable("outcome", LoanOutcome.REJECTED)
                            }
                            findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                        }
                    }else{
                        // Mostrar pantalla de error
                        Log.d("TumiDibursementFragment", "Error")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }

                }else{
                    // Mostrar pantalla de error
                    Log.d("TumiDibursementFragment", "Error")
                    val bundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                }
            }
        }
    }

    private suspend fun tumiPayDisbursement(idPrestamo: String){
        val formData = LoanTumiPayRequest(idPrestamo)

        val loanTumiPayResponse = withContext(Dispatchers.IO) {
            repository.getDataTumiPayDisbursement(formData)
        }

        Log.d("TumiDibursementFragment", "Loan tumipay: ${loanTumiPayResponse}")
        var bundle: Bundle

        if(loanTumiPayResponse != null){
            if(loanTumiPayResponse.prestamo.codigo == "200" && loanTumiPayResponse.prestamo.result == SUCCESSFUL_DISBURSEMENT_RESULT){
                bundle = Bundle().apply {
                    putSerializable("outcome", LoanOutcome.SUCCESS_TUMI)
                }
            }else{
                bundle = Bundle().apply {
                    putSerializable("outcome", LoanOutcome.REJECTED)
                }
            }
        }else{
            Log.d("BankDibursementFragment", "Error")
            bundle = Bundle().apply {
                putSerializable("outcome", LoanOutcome.REJECTED)
            }
        }
        // Mostrar pantalla resultante
        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
    }
}