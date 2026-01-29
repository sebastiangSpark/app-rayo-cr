package com.rayo.rayoxml.mx.ui.loan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.PaymentMethodAdapter
import com.rayo.rayoxml.databinding.FragmentPaymentMethodScreenBinding
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalInfoLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalLoadResponse
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalStepFourPlusSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepFiveLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFivePlusLoadRequest
import com.rayo.rayoxml.mx.services.Loan.PLPLoanStep4Request
import com.rayo.rayoxml.mx.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.mx.ui.renewal.RenewalFragment
import com.rayo.rayoxml.mx.viewModels.RenewalViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentMethodScreenFragment : Fragment() {

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

        repository = LoanRepository()

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("PaymentMethodScreenFragment", "Tipo de préstamo: ${loanType}")
            }
        }

        /*binding.btnNextStep.setOnClickListener {
            Log.d("PaymentMethodScreenFragment", "Ingresando a crear préstamo")
            (parentFragment as? FormFragment)?.goToNextStep()
        }
        binding.btnNextStep.setOnClickListener {
            Log.d("PaymentMethodScreenFragment", "Ingresando a editar préstamo")
            //(parentFragment as? RenewalFragment)?.goToNextStep()
            lifecycleScope.launch {
                when(loanType){
                    "MINI" -> runValidationsMini()
                    "RP" -> runValidationsRP()
                    "PLP" -> runValidationsPLP()
                }
            }
        }*/

        binding.btnNextStep.setOnClickListener {
            when (parentFragment) {
                is FormFragment -> {
                    Log.d("PaymentMethodScreenFragment", "Ingresando a crear préstamo")
                    (parentFragment as FormFragment).goToNextStep()
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
            }
        }
        renewalViewModel.proposalLoadRequest.observe(viewLifecycleOwner) { data ->
            data?.let {
                proposalRequest = it
                Log.d("PaymentMethodScreenFragment", "Datos de propuesta de renovación (request): ${proposalRequest}")
            }
        }

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("PaymentMethodScreenFragment", "Tipo de préstamo: ${loanType}")
            }
        }

        // Check tecnología
        renewalViewModel.technologyCheck.observe(viewLifecycleOwner) { option ->
            option?.let {
                technologyEnabled = it
                Log.d("PaymentMethodScreenFragment", "Datos de check de tecnología: ${technologyEnabled}")
            }
        }

        // Cuotas
        renewalViewModel.loanTerm.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanTerm = it
                Log.d("PaymentMethodScreenFragment", "Datos de plazo: ${loanTerm}")
            }
        }

        // Estado descuentos
        renewalViewModel.adminDiscount.observe(viewLifecycleOwner) { data ->
            data?.let {
                adminDiscount = it
                Log.d("PaymentMethodScreenFragment", "Datos de descuento administrativo: ${adminDiscount}")
            }
        }
        renewalViewModel.tenDiscount.observe(viewLifecycleOwner) { data ->
            data?.let {
                tenDiscount = it
                Log.d("PaymentMethodScreenFragment", "Datos de descuento 10: ${tenDiscount}")
            }
        }

        // Formulario PLP
        renewalViewModel.formId.observe(viewLifecycleOwner) { data ->
            data?.let {
                formId = it
                Log.d("PaymentMethodScreenFragment", "Datos de formulario PLP: ${formId}")
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

                                if(infoLoadResponse != null){
                                    if(infoLoadResponse.solicitud.codigo == "200"){
                                        Log.d("PaymentMethodScreenFragment", "Solicitud información OK")

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
                        Log.d("PaymentMethodScreenFragment", "Loan renewal information plus load data: ${loanRenewalProposalSubmitResponse}")

                        if (loanRenewalProposalInformation != null){
                            if(loanRenewalProposalInformation.solicitud.codigo == "200"){
                                Log.d("RenewalProposalFragment", "Solicitud información OK")

                                // Asignar opción de desembolso tumipay
                                if(loanRenewalProposalInformation.solicitud.desembolsoAuto != null &&
                                    loanRenewalProposalInformation.solicitud.desembolsoAuto.isNotEmpty()) renewalViewModel.setTumipayDisbursement(
                                    loanRenewalProposalInformation.solicitud.desembolsoAuto
                                )

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