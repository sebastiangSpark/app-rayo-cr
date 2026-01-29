package com.rayo.rayoxml.mx.ui.disbursement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentTumiDisbursementBinding
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.mx.services.Loan.LoanTumiPayRequest
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModelFactory
import com.rayo.rayoxml.mx.services.Loan.SolicitudStepFiveLoad
import com.rayo.rayoxml.mx.services.User.Solicitud
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.ui.dialogs.TumiDialogFragment
import com.rayo.rayoxml.mx.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TumiDisbursementFragment() : Fragment() {

    private lateinit var viewModel: UserViewModel

    private var _binding: FragmentTumiDisbursementBinding? = null
    private val binding get() = _binding!!

    private var formId: String? = null
    private lateinit var repository: LoanRepository

    private val SUCCESSFUL_FORM_RESULT: String = "La solicitud fue procesada exitosamente"
    private val SUCCESSFUL_DISBURSEMENT_RESULT: String = "OK"

    private var userData: Solicitud? = null
    private var personalData: LoanStepOneRequest? = null
    private var bankData: LoanStepTwoRequest? = null

    // viewmodel de datos de usuario
    private lateinit var loanViewModel: LoanStepOneViewModel
    private lateinit var validationViewModel: LoanValidationStepViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Asignar viewModel
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        _binding = FragmentTumiDisbursementBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTumiDisbursement.setOnClickListener {
            showCustomDialog()
        }

        // Mostrar opción alternativa de pago
        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.bankDisbursementFragment)
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
                Log.d("TumiDibursementFragment", "Formulario asignado user: $data")
                formId = data
            }
        }
        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("TumiDibursementFragment", "Datos personales user: ${data}")
                personalData = data
                // Cargar datos banco
                if(personalData != null){
                    if(personalData!!.celular.isNotEmpty()) binding.userBank.setText(personalData!!.celular)
                }
            }
        }
        viewModel.bankdData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("TumiDibursementFragment", "Datos bancarios user: ${data}")
                bankData = data
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

    private suspend fun runValidations(){
        Log.d("TumiDibursementFragment", "Formulario paso tumipay: $formId")
        Log.d("TumiDibursementFragment", "Plazo paso tumipay: ${bankData!!.plazoSeleccionado}")
        if(formId != null){
            val formData = LoanStepFiveLoadRequest(formId!!)

            val loanStepFiveLoadResponse = withContext(Dispatchers.IO) {
                repository.getDataStepFiveLoad(formData)
            }

            Log.d("TumiDibursementFragment", "Loan step five: ${loanStepFiveLoadResponse}")

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