package com.rayo.rayoxml.mx.ui.disbursement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rayo.rayoxml.databinding.FragmentBankDisbursementBinding
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalInfoLoadResponse
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFivePlusLoadResponse
import com.rayo.rayoxml.mx.services.Loan.LoanStepFivePlusSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModelFactory
import com.rayo.rayoxml.mx.services.Loan.PLPLoanStep5Request
import com.rayo.rayoxml.mx.services.Loan.SolicitudStepFiveLoad
import com.rayo.rayoxml.mx.services.User.Solicitud
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.ui.dialogs.BankDialogFragment
import com.rayo.rayoxml.mx.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.mx.ui.loan.FormFragment
import com.rayo.rayoxml.mx.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.mx.ui.renewal.RenewalFragment
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.mx.viewModels.RenewalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BankDisbursementFragment(/*private val viewModel: UserViewModel*/) : Fragment() {
    private lateinit var viewModel: UserViewModel

    private var _binding: FragmentBankDisbursementBinding? = null
    private val binding get() = _binding!!

    private var formId: String? = null
    private lateinit var repository: LoanRepository

    private val SUCCESSFUL_FORM_RESULT: String = "La solicitud fue procesada exitosamente"

    private var userData: Solicitud? = null
    private var personalData: LoanStepOneRequest? = null
    private var bankData: LoanStepTwoRequest? = null

    // viewmodel de datos de usuario
    //private lateinit var loanViewModel: LoanStepOneViewModel
    private lateinit var validationViewModel: LoanValidationStepViewModel

    // Variables para renovación

    //private lateinit var repository: LoanRepository

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    // Modelo compartido
    private val renewalViewModel: RenewalViewModel by activityViewModels()
    private lateinit var loanRenewalInfoLoadResponse: LoanRenewalInfoLoadResponse
    private lateinit var loanRpRenewalInfoLoadResponse: LoanStepFivePlusLoadResponse
    private var loanTerm: Int = 0
    private var loanType: String = ""
    private var technologyEnabled: Boolean = false
    private var tumipayEnabled: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankDisbursementBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = LoanRepository()

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        binding.buttonNext.setOnClickListener {
            showCustomDialog()
        }

        // Mostrar opción alternativa de pago
        binding.btnSkip.setOnClickListener {

            when (parentFragment) {
                is FormFragment -> findNavController().navigate(R.id.tumiDisbursementFragment)
                is RenewalFragment -> (parentFragment as RenewalFragment).goToNextStep()
            }
        }

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

        viewModel.formId.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("BankDisbursementFragment", "Formulario asignado user: $data")
                formId = data
            }
        }
        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("BankDisbursementFragment", "Datos personales user: ${data}")
                personalData = data
            }
        }
        viewModel.bankdData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("BankDisbursementFragment", "Datos bancarios user: ${data}")
                bankData = data
                // Cargar datos banco
                if(bankData != null){
                    if(bankData!!.banco.isNotEmpty()) binding.userBank.setText(bankData!!.banco)
                    if(bankData!!.referenciaBancaria.isNotEmpty()) binding.userBankAccount.setText(bankData!!.referenciaBancaria)
                }
            }
        }

        // Banco y cuenta en renovación
        viewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("BankDisbursementFragment", "Datos user: ${data}")
                userData = data

                if(userData!!.banco!!.isNotEmpty()) binding.userBank.setText(userData!!.banco)
                if(userData!!.numeroCuenta!!.isNotEmpty()) binding.userBankAccount.setText(userData!!.numeroCuenta)
            }
        }

        // botón Tumipay
        renewalViewModel.tumipayDisbursement.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                tumipayEnabled = data.toBooleanSI()
                Log.d("BankDisbursementFragment", "Datos opción tumipay ${data} - Mostrar? ${tumipayEnabled}")
                // Habilitar botón si aplica
                if(tumipayEnabled) binding.btnSkip.visibility = View.VISIBLE
            }
        }

        // Obtener ViewModel compartido con la actividad
        val repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanValidationStepViewModelFactory(repository, preferencesManager)
        validationViewModel = ViewModelProvider(requireActivity(), factory)[LoanValidationStepViewModel::class.java]

        validationViewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.codigo == "200") {
                formId = data.formulario
                Log.d("BankDisbursementFragment", "Formulario asignado validation: $data")
            }
        }

        // observers renovar
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
        }
        renewalViewModel.loanTerm.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanTerm = it
                Log.d("PaymentMethodScreenFragment", "Datos de plazo: ${loanTerm}")
            }
        }
        // fin observers renovar

        // Listener de modal de aprobación desembolso
        parentFragmentManager.setFragmentResultListener("bankDialogResult", viewLifecycleOwner) { _, bundle ->

            val result = bundle.getString("result")
            Log.d("BankDisbursementFragment", "Respuesta: $result")

            // Detectar si el flujo es de renovación
            if( loanType.isNullOrEmpty() ){
                Log.d("BankDisbursementFragment", "Ingresando a crear préstamo")

                if (result == "approved") {
                    Log.d("BankDisbursementFragment", "Aprobado desembolso")
                    // Continuar si se aprobó en modal
                    viewLifecycleOwner.lifecycleScope.launch {
                        runValidations()
                    }
                } else if (result == "cancelled") {
                    Log.d("BankDisbursementFragment", "Cancelado desembolso")
                }
            }else{
                Log.d("BankDisbursementFragment", "Ingresando a renovar préstamo")
                if (result == "approved") {
                    Log.d("BankDisbursementFragment", "Aprobado desembolso en renovación")
                    lifecycleScope.launch {
                        when(loanType){
                            "MINI" -> submitLoanMini()
                            "RP" -> submitLoanRP()
                            "PLP" -> submitLoanPLP()
                        }
                    }
                }
                else if (result == "cancelled") {
                    Log.d("BankDisbursementFragment", "Cancelado desembolso en renovación")
                }
                //(parentFragment as? RenewalFragment)?.goToNextStep()
            }
        }
    }

    fun String.toBooleanSI(): Boolean {
        return this.equals("SI", ignoreCase = true)
    }

    private fun showCustomDialog() {
        val dialogFragment = BankDialogFragment()
        dialogFragment.show(parentFragmentManager, "CustomDialogFragment")
    }

    private suspend fun runValidations(){
        Log.d("BankDisbursementFragment", "Formulario paso 5: $formId")
        Log.d("BankDisbursementFragment", "Plazo paso 5: ${bankData!!.plazoSeleccionado}")
        if(formId != null){
            val formData = LoanStepFiveLoadRequest(formId!!)

            val loanStepFiveLoadResponse = withContext(Dispatchers.IO) {
                repository.getDataStepFiveLoad(formData)
            }

            Log.d("BankDisbursementFragment", "Loan step five: ${loanStepFiveLoadResponse}")

            if (loanStepFiveLoadResponse != null){
                if(loanStepFiveLoadResponse.solicitud.codigo == "200"){

                    val data: SolicitudStepFiveLoad = loanStepFiveLoadResponse.solicitud

                    if(data.formulario.isNotEmpty()){
                        // STEP 5
                        if(bankData != null){
                            val formDataStep5 = LoanStepFiveSubmitRequest(
                                formulario = data.formulario,
                                plazoSeleccionado = bankData!!.plazoSeleccionado
                            )
                            val loanStepFiveSubmitResponse = withContext(Dispatchers.IO) {
                                repository.getDataStepFiveSubmit(formDataStep5)
                            }
                            if (loanStepFiveSubmitResponse != null) {
                                if(loanStepFiveSubmitResponse.solicitud.codigo == "200" && loanStepFiveSubmitResponse.solicitud.result == SUCCESSFUL_FORM_RESULT){
                                    // Mostrar pantalla de confirmación
                                    Log.d("BankDisbursementFragment", "Préstamo creado: ${loanStepFiveSubmitResponse.solicitud.prestamo}")
                                    val bundle = Bundle().apply {
                                        putSerializable("outcome", LoanOutcome.SUCCESS_BANK)
                                    }
                                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                                }else{
                                    // Mostrar pantalla de error
                                    Log.d("BankDisbursementFragment", "Error")
                                    val bundle = Bundle().apply {
                                        putSerializable("outcome", LoanOutcome.REJECTED)
                                    }
                                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                                }
                            }
                        }else{
                            Log.d("BankDisbursementFragment", "User data null")
                            val bundle = Bundle().apply {
                                putSerializable("outcome", LoanOutcome.REJECTED)
                            }
                            findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                        }
                    }else{
                        // Mostrar pantalla de error
                        Log.d("BankDisbursementFragment", "Error")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }

                }else{
                    // Mostrar pantalla de error
                    Log.d("BankDisbursementFragment", "Error")
                    val bundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                }
            }
        }
    }

    // Funciones renovación

    private suspend fun submitLoanMini(){
        if( !::loanRenewalInfoLoadResponse.isInitialized){
            Log.d("RenewalProposalFragment", "loanRenewalInfoLoadResponse no inicializada")
            return
        }

        val formData = LoanStepFiveSubmitRequest(
            formulario = loanRenewalInfoLoadResponse.solicitud.formulario,
            plazoSeleccionado = loanRenewalInfoLoadResponse.solicitud.plazo.toDouble().toInt()
        )

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val infoSubmitResponse = withContext(Dispatchers.IO) {
                repository.getDataStepFiveSubmit(formData)
            }

            Log.d("RenewalProposalFragment", "Loan renewal info load: ${infoSubmitResponse}")

            if(infoSubmitResponse != null){
                if(infoSubmitResponse.solicitud.codigo == "200"){
                    Log.d("RenewalProposalFragment", "Solicitud información OK")

                    // Continuar a sigte pantalla
                    Log.d("RenewalProposalFragment", "Cargando pantalla de confirmación de préstamo")

                    //(parentFragment as? RenewalFragment)?.goToNextStep()

                    // Renovar préstamo
                    if (infoSubmitResponse != null) {
                        if(infoSubmitResponse.solicitud.codigo == "200" && infoSubmitResponse.solicitud.result == SUCCESSFUL_FORM_RESULT){
                            // Mostrar pantalla de confirmación
                            Log.d("RenewalProposalFragment", "Préstamo renovado: ${infoSubmitResponse.solicitud.prestamo}")
                            val bundle = Bundle().apply {
                                putSerializable("outcome", LoanOutcome.SUCCESS_BANK)
                            }
                            findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                        }else{
                            // Mostrar pantalla de error
                            Log.d("RenewalProposalFragment", "Error")
                            val bundle = Bundle().apply {
                                putSerializable("outcome", LoanOutcome.REJECTED)
                            }
                            findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                        }
                    }

                }else{
                    Log.d("RenewalProposalFragment", "Error Loan renewal info load: ${infoSubmitResponse}")
                    Toast.makeText(requireContext(), "Error: ${infoSubmitResponse.solicitud.result}", Toast.LENGTH_LONG).show()
                    // Mostrar pantalla de error
                    Log.d("RenewalProposalFragment", "Error")
                    val bundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                }
            }else{
                Log.d("RenewalProposalFragment", "infoLoadResponse nulo")
            }

            loadingDialog.dismiss() // ocultar loading

        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("ProposalConfirmationFragment", "Error obteniendo datos", e)
        }
    }

    private suspend fun submitLoanRP(){
        if( !::loanRpRenewalInfoLoadResponse.isInitialized){
            Log.d("RenewalProposalFragment", "loanRenewalInfoLoadResponse no inicializada")
            return
        }

        val formData = LoanStepFivePlusSubmitRequest(
            checkTecnologia = technologyEnabled,
            formulario = loanRpRenewalInfoLoadResponse.solicitud.formulario!!,
            plazoSeleccionado = loanTerm
        )

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val infoSubmitResponse = withContext(Dispatchers.IO) {
                repository.getDataStepFivePlusSubmit(formData)
            }

            Log.d("RenewalProposalFragment", "Loan rp renewal info load: ${infoSubmitResponse}")

            if(infoSubmitResponse != null){
                if(infoSubmitResponse.solicitud.codigo == "200"){
                    Log.d("RenewalProposalFragment", "Solicitud información OK")

                    // Continuar a sigte pantalla
                    Log.d("RenewalProposalFragment", "Cargando pantalla de confirmación de préstamo RP")

                    //(parentFragment as? RenewalFragment)?.goToNextStep()

                    // Renovar préstamo
                    if (infoSubmitResponse.solicitud.result == SUCCESSFUL_FORM_RESULT) {
                        // Mostrar pantalla de confirmación
                        Log.d("RenewalProposalFragment", "Préstamo RP renovado: ${infoSubmitResponse.solicitud.prestamo}")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.SUCCESS_BANK)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }else{
                        // Mostrar pantalla de error
                        Log.d("RenewalProposalFragment", "Error")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }

                }else{
                    Log.d("RenewalProposalFragment", "Error Loan renewal info load: ${infoSubmitResponse}")
                    Toast.makeText(requireContext(), "Error: ${infoSubmitResponse.solicitud.result}", Toast.LENGTH_LONG).show()
                    // Mostrar pantalla de error
                    Log.d("RenewalProposalFragment", "Error")
                    val bundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                }
            }else{
                Log.d("RenewalProposalFragment", "infoLoadResponse nulo")
                // Mostrar pantalla de error
                Log.d("RenewalProposalFragment", "Error")
                val bundle = Bundle().apply {
                    putSerializable("outcome", LoanOutcome.REJECTED)
                }
                findNavController().navigate(R.id.loanOutcomeFragment, bundle)
            }

            loadingDialog.dismiss() // ocultar loading

        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("ProposalConfirmationFragment", "Error obteniendo datos", e)
            // Mostrar pantalla de error
            Log.d("RenewalProposalFragment", "Error")
            val bundle = Bundle().apply {
                putSerializable("outcome", LoanOutcome.REJECTED)
            }
            findNavController().navigate(R.id.loanOutcomeFragment, bundle)
        }
    }

    private suspend fun submitLoanPLP(){

        val formData = PLPLoanStep5Request(
            formulario = formId.toString(),
            plazoSeleccionado = 180
        )

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val plpLoanStep5Response = withContext(Dispatchers.IO) {
                repository.getDataPlpStep5(formData)
            }

            Log.d("BankDisbursementFragment", "PLP loan step 5 Data: ${plpLoanStep5Response}")

            if (plpLoanStep5Response != null) {
                if (plpLoanStep5Response.solicitud.codigo == "200") {

                    // Actualizar request y response en viewModel
                    //renewalViewModel.setProposalSubmitRequestData(formData)
                    //renewalViewModel.setProposalSubmitResponseData(plpLoanStep4Response)

                    Log.d("BankDisbursementFragment", "Solicitud información paso 5 OK")

                    // Continuar
                    // Renovar préstamo
                    if (plpLoanStep5Response.solicitud.result == SUCCESSFUL_FORM_RESULT) {
                        // Mostrar pantalla de confirmación
                        Log.d("BankDisbursementFragment", "Préstamo PLP renovado: ${plpLoanStep5Response.solicitud.formulario}")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.SUCCESS_BANK)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }else{
                        // Mostrar pantalla de error
                        Log.d("BankDisbursementFragment", "Error")
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }

                } else {
                    Log.d("BankDisbursementFragment", "plpLoanStep5Response nulo")
                    val bundle = Bundle().apply {
                        putSerializable("outcome", LoanOutcome.REJECTED)
                    }
                    findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                }

                loadingDialog.dismiss() // ocultar loading
            }else {
                Log.d("BankDisbursementFragment", "plpLoanStep5Response nulo")
            }

        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("BankDisbursementFragment", "Error obteniendo datos", e)
        }
    }
}