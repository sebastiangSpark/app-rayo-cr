package com.rayo.rayoxml.co.ui.loan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.PaymentMethodAdapter
import com.rayo.rayoxml.co.adapters.StepFragment
import com.rayo.rayoxml.databinding.FragmentPaymentMethodScreenBinding
import com.rayo.rayoxml.co.services.Loan.LoanRenewalInfoLoadRequest
import com.rayo.rayoxml.co.services.Loan.LoanRenewalProposalLoadRequest
import com.rayo.rayoxml.co.services.Loan.LoanRenewalProposalLoadResponse
import com.rayo.rayoxml.co.services.Loan.LoanRenewalProposalSubmitRequest
import com.rayo.rayoxml.co.services.Loan.LoanRenewalStepFourPlusSubmitRequest
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.LoanStepFiveLoadRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepFivePlusLoadRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.co.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.co.services.Loan.PLPLoanStep4Request
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.co.ui.renewal.RenewalFragment
import com.rayo.rayoxml.co.viewModels.RenewalViewModel
import com.rayo.rayoxml.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentMethodScreenFragment : Fragment(),
    StepFragment {
    override fun getStepTitle() = "Confirmación de la Propuesta"
    private var _binding: FragmentPaymentMethodScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialogPaymentMethodScreen: EditText

    // Modelo compartido
    private val renewalViewModel: RenewalViewModel by activityViewModels()
    private lateinit var proposalResponse: LoanRenewalProposalLoadResponse
    private lateinit var proposalRequest: LoanRenewalProposalLoadRequest

    private lateinit var repository: LoanRepository

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    // Habilitar débito automático
    private var debitEnabled: Boolean = false
    private var technologyEnabled: Boolean = false
    private var adminDiscount: String = ""
    private var tenDiscount: String = ""
    private var loanTerm: Int = 0
    private var loanType: String = ""
    private var fianzaRP: Int = 0 // Obtener
    private var formId: String? = null
    private var totalLoans: Int = -1

    private lateinit var viewModel: UserViewModel

    private var isLoanTypeLoaded = false
    private var isProposalResponseLoaded = false
    private var isProposalRequestLoaded = false
    private var isTechnologyLoaded = false
    private var isLoanTermLoaded = false
    private var isAdminDiscountLoaded = false
    private var isTenDiscountLoaded = false
    private var isFormIdLoaded = false
    private var isTotalLoansLoaded = false
    private var hasSubmitted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPaymentMethodScreenBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asignar viewModel
        val userRepository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(userRepository, preferencesManager)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        repository = LoanRepository()

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("PaymentMethodScreenFragment", "Tipo de préstamo: ${loanType}")
                isLoanTypeLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }

        binding.btnNextStep.setOnClickListener {
            when (parentFragment) {
                is FormFragment -> {
                    Log.d("PaymentMethodScreenFragment", "Ingresando a crear préstamo")
                    view?.post {
                        (parentFragment as? FormFragment)?.goToNextStep()
                    }
                }
                is RenewalFragment ->{
                    Log.d("PaymentMethodScreenFragment", "Ingresando a renovar préstamo")
                    //(parentFragment as? RenewalFragment)?.goToNextStep()
                    lifecycleScope.launch {
                        when(loanType){
                            "MINI" -> runValidationsMini()
                            "RP" -> runValidationsRP()
                            "PLP" -> runValidationsPLP()
                        }
                    }
                }
            }
        }

        dialogPaymentMethodScreen = view.findViewById(R.id.dialog_payment_method_screen)

        dialogPaymentMethodScreen.setOnClickListener {
            showBottomSheetDialogPayment()
        }

        // Observer datos compartidos
        renewalViewModel.proposalLoadResponse.observe(viewLifecycleOwner) { data ->
            data?.let {
                proposalResponse = it
                Log.d("PaymentMethodScreenFragment", "Datos de propuesta de renovación (response): ${proposalResponse}")
                isProposalResponseLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }
        renewalViewModel.proposalLoadRequest.observe(viewLifecycleOwner) { data ->
            data?.let {
                proposalRequest = it
                Log.d("PaymentMethodScreenFragment", "Datos de propuesta de renovación (request): ${proposalRequest}")
                isProposalRequestLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }

        // Total préstamos
        renewalViewModel.totalLoans.observe(viewLifecycleOwner) { data ->
            data?.let {
                totalLoans = it
                Log.d("PaymentMethodScreenFragment", "Total préstamos: ${totalLoans}")
                isTotalLoansLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }

        // Check tecnología
        renewalViewModel.technologyCheck.observe(viewLifecycleOwner) { option ->
            option?.let {
                technologyEnabled = it
                Log.d("PaymentMethodScreenFragment", "Datos de check de tecnología: ${technologyEnabled}")
                isTechnologyLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }

        // Cuotas
        renewalViewModel.loanTerm.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanTerm = it
                Log.d("PaymentMethodScreenFragment", "Datos de plazo: ${loanTerm}")
                isLoanTermLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }
        // Cuotas usuario nuevo
        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            data?.let {
                if(data.plazoSeleccionado > 0) loanTerm = data.plazoSeleccionado
                Log.d("PaymentMethodScreenFragment", "Datos de plazo usuario nuevo: ${loanTerm}")
                isLoanTermLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }

        // Estado descuentos
        renewalViewModel.adminDiscount.observe(viewLifecycleOwner) { data ->
            data?.let {
                adminDiscount = it
                Log.d("PaymentMethodScreenFragment", "Datos de descuento administrativo: ${adminDiscount}")
                isAdminDiscountLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }
        renewalViewModel.tenDiscount.observe(viewLifecycleOwner) { data ->
            data?.let {
                tenDiscount = it
                Log.d("PaymentMethodScreenFragment", "Datos de descuento 10: ${tenDiscount}")
                isTenDiscountLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }

        // Formulario PLP
        renewalViewModel.formId.observe(viewLifecycleOwner) { data ->
            data?.let {
                formId = it
                Log.d("PaymentMethodScreenFragment", "Datos de formulario PLP: ${formId}")
                isFormIdLoaded = true
                lifecycleScope.launch {
                    checkAllDataLoaded()
                }
            }
        }
    }

    suspend fun checkAllDataLoaded() {
        if (hasSubmitted) return // Ejecutar sólo una vez
        if ( isLoanTypeLoaded && isTechnologyLoaded && isLoanTermLoaded && isTotalLoansLoaded &&
            ( isProposalResponseLoaded && isProposalRequestLoaded && isAdminDiscountLoaded &&
             isTenDiscountLoaded && (isFormIdLoaded || loanType != "PLP")) || totalLoans == 0 )
        {
            Log.d("PaymentMethodScreenFragment", "✅ All data loaded, submitting...")
            // Validar si aplica opción débito (3er préstamo)
            if(totalLoans < 3){
                Log.d("PaymentMethodScreenFragment", "Tiene menos de 3 préstamos, omitir opción de seleccionar débito automático")
                /*if (loadingDialog.isAdded) {
                    loadingDialog.dismiss()
                }*/
                //delay(500)
                hasSubmitted = true
                binding.btnNextStep.performClick()
            }
        }
    }

    private fun showBottomSheetDialogPayment() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_payment_method_dialog, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewPaymentsMethod)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = PaymentMethodAdapter { selectedType ->
            binding.dialogPaymentMethodScreen.setText(selectedType)
            Log.d("PaymentMethodScreenFragment", "Opción de pagos seleccionada: ${selectedType}")
            debitEnabled = selectedType == "Débito Automático"
            dialog.dismiss()
        }

        dialog.show()
    }

    private suspend fun runValidationsMini(){
        val formData = LoanRenewalProposalSubmitRequest(
            checkTecnologia = technologyEnabled,
            plazoSeleccionado = loanTerm,
            showModal = false,
            debito = debitEnabled,
            archivo = null,
            archivoContentType = null,
            archivoName = null,
            descuento10 = tenDiscount,
            descuentoAdministracion = adminDiscount,
            idContacto = proposalRequest.idContacto,
            montoDescuento = proposalResponse.solicitud.descuento,
            porcentajeDescuento = proposalResponse.solicitud.porcentajeDescuento.toInt(),
            propuestaSugerida = proposalResponse.solicitud.propuestaSugerida.toInt()
        )

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        /*if (!loadingDialog.isAdded && !loadingDialog.isVisible) {
            loadingDialog.show(parentFragmentManager, "LoadingDialog")
        }*/

        try {
            val loanRenewalProposalSubmitResponse = withContext(Dispatchers.IO) {
                repository.getDataRenewalProposalSubmit(formData)
            }

            Log.d("PaymentMethodScreenFragment", "Loan renewal proposal submit data: ${loanRenewalProposalSubmitResponse}")

            if (loanRenewalProposalSubmitResponse != null) {
                if(loanRenewalProposalSubmitResponse.solicitud.codigo == "200"){

                    // Actualizar request y response en viewModel
                    renewalViewModel.setProposalSubmitRequestData(formData)
                    renewalViewModel.setProposalSubmitResponseData(loanRenewalProposalSubmitResponse)

                    Log.d("PaymentMethodScreenFragment", "Solicitud renovación OK")
                    // Continuar
                    if(loanRenewalProposalSubmitResponse.solicitud.formulario?.isNotEmpty() == true){
                        val newFormData = LoanStepFiveLoadRequest(
                            formulario = loanRenewalProposalSubmitResponse.solicitud.formulario
                        )

                        val loanStepFiveLoadResponse = withContext(Dispatchers.IO) {
                            repository.getDataStepFiveLoad(newFormData)
                        }

                        Log.d("PaymentMethodScreenFragment", "Loan renewal form load (STEP5): ${loanStepFiveLoadResponse}")

                        if(loanStepFiveLoadResponse != null){
                            if(loanStepFiveLoadResponse.solicitud.codigo == "200"){

                                Log.d("PaymentMethodScreenFragment", "Solicitud de formulario OK")

                                // Actualizar request y response en viewModel
                                renewalViewModel.setFormRequest(newFormData)
                                renewalViewModel.setFormResponseData(loanStepFiveLoadResponse)

                                // continuar
                                val InfoFormData = LoanRenewalInfoLoadRequest(
                                    formulario = loanStepFiveLoadResponse.solicitud.formulario
                                )

                                val infoLoadResponse = withContext(Dispatchers.IO) {
                                    repository.getDataRenewalInfoLoad(InfoFormData)
                                }

                                Log.d("PaymentMethodScreenFragment", "Loan renewal info load: ${infoLoadResponse}")
                                loadingDialog.dismiss() // ocultar loading

                                if(infoLoadResponse != null){
                                    if(infoLoadResponse.solicitud.codigo == "200"){
                                        Log.d("PaymentMethodScreenFragment", "Solicitud información OK")

                                        // Asignar datos para renovación
                                        val bankDataRequest = LoanStepTwoRequest(
                                            formulario = infoLoadResponse.solicitud.formulario,
                                            departamento = "",
                                            ciudadDepartamento = "",
                                            direccionExacta = "",
                                            banco = "",
                                            tipoCuenta = "",
                                            referenciaBancaria = "",
                                            referenciaBancaria2 = "",
                                            plazoSeleccionado = infoLoadResponse.solicitud.plazo.toDouble().toInt(),
                                            telefonoEmpresa = "",
                                            contadorActualizado = 1
                                        )
                                        viewModel.setBankData(bankDataRequest)

                                        val personalDataRequest = LoanStepOneRequest(
                                            nombre = "",
                                            primerApellido = "",
                                            numeroDocumento = "",
                                            fechaNacimiento = "",
                                            celular = infoLoadResponse.solicitud.celular,
                                            empleadoFormal = "",
                                            nombreCuentaBancaria = "",
                                            correoElectronico = "",
                                            tipoDocumento = "",
                                            plazoSeleccionado = infoLoadResponse.solicitud.plazo.toDouble().toInt(),
                                            valorSeleccionado = 0,
                                            fechaExpedicion = "",
                                            sessionId = "",
                                            genero = ""
                                        )

                                        lifecycleScope.launch {
                                            //viewModel.setPersonalData(personalDataRequest)
                                            //viewModel.setPhone(infoLoadResponse.solicitud.celular)
                                            //viewModel.setFormId(infoLoadResponse.solicitud.formulario)

                                            renewalViewModel.setFormId(infoLoadResponse.solicitud.formulario)
                                            renewalViewModel.setLoanTerm(infoLoadResponse.solicitud.plazo.toDouble().toInt())
                                        }

                                        // Asignar opción de desembolso tumipay
                                        if(loanStepFiveLoadResponse.solicitud.desembolsoAuto != null &&
                                            loanStepFiveLoadResponse.solicitud.desembolsoAuto.isNotEmpty()) renewalViewModel.setTumipayDisbursement(
                                            loanStepFiveLoadResponse.solicitud.desembolsoAuto
                                        )
                                        //Log.d("PaymentMethodScreenFragment", "Datos opción tumipay ${loanStepFiveLoadResponse.solicitud.desembolsoAuto} - Mostrar? ${loanStepFiveLoadResponse.solicitud.desembolsoAuto.equals("SI", ignoreCase = true)}")

                                        // Actualizar request y response en viewModel
                                        renewalViewModel.setInfoRequestData(InfoFormData)
                                        renewalViewModel.setInfoResponseData(infoLoadResponse)

                                        // Continuar a sigte pantalla
                                        Log.d("PaymentMethodScreenFragment", "Cargando pantalla de confirmación de propuesta")

                                        if (loadingDialog.isAdded) {
                                            loadingDialog.dismiss()
                                        }
                                        (parentFragment as? RenewalFragment)?.goToNextStep()

                                    }else{
                                        Log.d("PaymentMethodScreenFragment", "Error Loan renewal info load: ${loanStepFiveLoadResponse}")
                                    }
                                }else{
                                    Log.d("PaymentMethodScreenFragment", "infoLoadResponse nulo")
                                }
                            } else{
                                Log.d("PaymentMethodScreenFragment", "Error loan renewal form load (STEP5): ${loanStepFiveLoadResponse}")
                            }
                        }
                    }else{
                        Log.d("PaymentMethodScreenFragment", "Formulario renovación no retornado")
                    }

                }else{
                    // Pantalla de error
                    Log.d("PaymentMethodScreenFragment", "Error loan renewal proposal submit: ${loanRenewalProposalSubmitResponse}")
                    loadingDialog.dismiss() // ocultar loading
                }
            }else{
                Log.d("PaymentMethodScreenFragment", "loanRenewalProposalSubmitResponse nulo")
                loadingDialog.dismiss() // ocultar loading
            }
            //loadingDialog.dismiss() // ocultar loading
        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("PaymentMethodScreenFragment", "Error obteniendo datos", e)
        }
    }

    private suspend fun runValidationsRP(){

        val formData = LoanRenewalStepFourPlusSubmitRequest(
            formulario = "",
            checkTecnologia = technologyEnabled,
            plazoSeleccionado = loanTerm,
            showModal = false,
            debito = debitEnabled,
            descuento10 = tenDiscount,
            descuentoAdministracion = adminDiscount,
            idContacto = proposalRequest.idContacto,
            montoDescuento = proposalResponse.solicitud.descuento,
            propuestaSugerida = proposalResponse.solicitud.propuestaSugerida,
            fianzaRP = fianzaRP,
            /*
            archivo = null,
            archivoContentType = null,
            archivoName = null,
            porcentajeDescuento = proposalResponse.solicitud.porcentajeDescuento.toInt(),
             */
        )

        Log.d("PaymentMethodScreenFragment", "DATOS RP: ${formData}")
        //return

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val loanRenewalProposalSubmitResponse = withContext(Dispatchers.IO) {
                repository.getDataRenewalStepFourPlusSubmit(formData)
            }

            Log.d("PaymentMethodScreenFragment", "Loan renewal proposal submit data: ${loanRenewalProposalSubmitResponse}")

            if (loanRenewalProposalSubmitResponse != null) {
                if(loanRenewalProposalSubmitResponse.solicitud.codigo == "200"){

                    // Actualizar request y response en viewModel
                    //renewalViewModel.setProposalSubmitRequestData(formData)
                    //renewalViewModel.setProposalSubmitResponseData(loanRenewalProposalSubmitResponse)

                    Log.d("PaymentMethodScreenFragment", "Solicitud renovación OK")
                    // Continuar
                    if(loanRenewalProposalSubmitResponse.solicitud.formulario?.isNotEmpty() == true){

                        // Paso adicional - informacion

                        val formInformacion = LoanStepFivePlusLoadRequest(
                            loanRenewalProposalSubmitResponse.solicitud.formulario
                        )

                        val loanRenewalProposalInformation = withContext(Dispatchers.IO) {
                            repository.getDataStepFivePlusLoadTwo(formInformacion)
                        }
                        Log.d("PaymentMethodScreenFragment", "Loan renewal information plus load data: ${loanRenewalProposalInformation}")

                        if (loanRenewalProposalInformation != null){
                            if(loanRenewalProposalInformation.solicitud.codigo == "200"){
                                Log.d("PaymentMethodScreenFragment", "Solicitud información OK")

                                // Asignar opción de desembolso tumipay
                                if(loanRenewalProposalInformation.solicitud.desembolsoAuto != null &&
                                    loanRenewalProposalInformation.solicitud.desembolsoAuto.isNotEmpty()) {
                                    Log.d("PaymentMethodScreenFragment", "Asignando opción tumipay: ${loanRenewalProposalInformation.solicitud.desembolsoAuto}")
                                    renewalViewModel.setTumipayDisbursement(
                                        loanRenewalProposalInformation.solicitud.desembolsoAuto
                                    )
                                }else{
                                    Log.d("PaymentMethodScreenFragment", "Opción tumipay deshabilitada: ${loanRenewalProposalInformation.solicitud.desembolsoAuto}")
                                }

                                // Paso adicional - formulario rp
                                val formRP = LoanStepFivePlusLoadRequest(
                                    loanRenewalProposalInformation.solicitud.formulario
                                )

                                val loanRenewalProposalForm = withContext(Dispatchers.IO) {
                                    repository.getDataStepFivePlusLoad(formRP)
                                }
                                Log.d("PaymentMethodScreenFragment", "Loan renewal form plus load data: ${loanRenewalProposalForm}")
                                if (loanRenewalProposalForm != null){
                                    if(loanRenewalProposalForm.solicitud.codigo == "200"){
                                        Log.d("RenewalProposalFragment", "Solicitud form OK")

                                        // Actualizar request y response en viewModel
                                        renewalViewModel.setInfoRpRequestData(formRP)
                                        renewalViewModel.setInfoRpResponseData(loanRenewalProposalForm)

                                        // Asignar formulario
                                        //renewalViewModel.setFormId(formRP.formulario)

                                        // Continuar a sigte pantalla
                                        Log.d("PaymentMethodScreenFragment", "Cargando pantalla de confirmación de propuesta")

                                        (parentFragment as? RenewalFragment)?.goToNextStep()
                                    }
                                }
                            }
                        }
                    }else{
                        Log.d("PaymentMethodScreenFragment", "Formulario renovación no retornado")
                    }

                }else{
                    // Pantalla de error
                    Log.d("PaymentMethodScreenFragment", "Error loan renewal proposal submit: ${loanRenewalProposalSubmitResponse}")
                }
            }else{
                Log.d("PaymentMethodScreenFragment", "loanRenewalProposalSubmitResponse nulo")
            }

            loadingDialog.dismiss() // ocultar loading

        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("PaymentMethodScreenFragment", "Error obteniendo datos", e)
        }
    }

    private suspend fun runValidationsPLP(){

        val formData = PLPLoanStep4Request(
            formulario = formId.toString()
        )

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val plpLoanStep4Response = withContext(Dispatchers.IO) {
                repository.getDataPlpStep4(formData)
            }

            Log.d("RenewalPlpStep4Fragment", "PLP loan step 4 Data: ${plpLoanStep4Response}")

            if (plpLoanStep4Response != null) {
                if (plpLoanStep4Response.solicitud.codigo == "200") {

                    // Actualizar request y response en viewModel
                    //renewalViewModel.setProposalSubmitRequestData(formData)
                    //renewalViewModel.setProposalSubmitResponseData(plpLoanStep4Response)

                    Log.d("RenewalPlpStep4Fragment", "Solicitud información paso 4 OK")

                    // Continuar
                    (parentFragment as? RenewalFragment)?.goToNextStep()

                } else {
                    Log.d("RenewalPlpStep4Fragment", "RenewalPlpStep4Fragment nulo")
                }

                loadingDialog.dismiss() // ocultar loading
            }else {
                Log.d("RenewalProposalFragment", "loanRenewalProposalSubmitResponse nulo")
            }

        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("PaymentMethodScreenFragment", "Error obteniendo datos", e)
        }
    }
}