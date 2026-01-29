package com.rayo.rayoxml.cr.ui.renewal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.rayo.rayoxml.CR.ui.renewal.custombutton.ButtonValue
import com.rayo.rayoxml.cr.ui.renewal.custombutton.CustomButtonSelector
import com.rayo.rayoxml.cr.adapters.NeedHelpAdapter

import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.adapters.LoanPaymentAdapter
import com.rayo.rayoxml.databinding.FragmentRenewalProposalBinding

import com.rayo.rayoxml.cr.models.PaymentDate
import com.rayo.rayoxml.cr.services.Auth.LoginResponse
import com.rayo.rayoxml.cr.services.Auth.Prestamo
import com.rayo.rayoxml.cr.services.Loan.CompleteLoanMiniRenewalRequest
import com.rayo.rayoxml.cr.services.Loan.CompleteLoanRpRenewalRequest
import com.rayo.rayoxml.cr.services.Loan.CreateLoanMiniRenewalRequest
import com.rayo.rayoxml.cr.services.Loan.CreateLoanMiniRenewalResponse
import com.rayo.rayoxml.cr.services.Loan.CreateLoanRpRenewalRequest
import com.rayo.rayoxml.cr.services.Loan.CreateLoanRpRenewalResponse
import com.rayo.rayoxml.cr.services.Loan.LoanRenewViewModel
import com.rayo.rayoxml.cr.services.Loan.LoanRenewalViewModelFactory
import com.rayo.rayoxml.cr.services.Loan.LoanRepository
import com.rayo.rayoxml.cr.services.Loan.PLPLoanStep3Request
//import com.rayo.rayoxml.cr.services.User.Prestamo
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.cr.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.cr.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.utils.CreditInformation
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.CreditParameters
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.cr.viewModels.RenewalViewModel
import com.rayo.rayoxml.utils.DESCUENTOS
import com.rayo.rayoxml.utils.DESCUENTOS_RP
import com.rayo.rayoxml.utils.MONTOS
import com.rayo.rayoxml.utils.MontoDisponible
import com.rayo.rayoxml.utils.RangoMonto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.roundToInt

class RenewalProposalFragment(/*private val userParamsViewModel: UserViewModel*/) : Fragment() {

    private var _binding: FragmentRenewalProposalBinding? = null
    private val binding get() = _binding!!
    private lateinit var customButtonSelector: CustomButtonSelector
    // Comprobante de ingresos
    private var base64File: String? = null
    private var base64FileName: String? = null
    private var base64FileType: String? = null
    private var fileSelected = false
    private val fileSizeLimit: Long = 25
    private lateinit var editTextNeedHelp: EditText
    private lateinit var errorNeedHelp: TextView
    private lateinit var textViewNeedHelp: TextView
    // Modelo compartido
    private val renewalViewModel: RenewalViewModel by activityViewModels()

    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
    private val countryCurrency = "CRC"
    private val currencySymbol = "₡"

    private var selectedCountry: String = ""
    private var creditParams: CreditParameters? = null
    private var creditInfo: CreditInformation? = null

    // Recyclerview de pagos
    private var paymentDates = mutableListOf<PaymentDate>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanPaymentAdapter

    // viewmodel de datos de usuario
    //private lateinit var validationViewModel: LoanValidationStepViewModel

    private var formId: String = ""

    // Check de costo de tecnología
    private var technologyCheck = false

    // Variables globales de simulador de crédito
    private var loanValue = 0;
    private var loanTerm = 1;

    // Datos api
    //private lateinit var repository: LoanRepository
    private lateinit var loanRepository: LoanRepository

    //private val SUCCESSFUL_FORM_RESULT: String = "Formulario OK"

    //private var userData: Solicitud? = null
    //private var personalData: LoanStepOneRequest? = null

    // Datos de propuesta
    lateinit var loanRenewalResponse: CreateLoanMiniRenewalResponse
    lateinit var loanRpRenewalResponse: CreateLoanRpRenewalResponse

    //
    private var proposalValue: Double = 0.0
    private var userData: LoginResponse? = null
    private var hasMora: Boolean = false
    private lateinit var userPreferencesManager: PreferencesManager
    private lateinit var loanPreferencesManager: PreferencesManager
    private var loanType: String = ""
    //private var adminDiscountString: String = ""
    //private var tenDiscountString: String = ""
    private var discountRateString: String = ""
    //private var userScore: String = "" // A+, A
    private var scoreExperianString: String = "0" // 500, 700

    private var plpSept3Data: PLPLoanStep3Request? = null

    private lateinit var uploadDocumentContainer: FrameLayout
    private lateinit var uploadDocumentContainerTitle: TextView
    private lateinit var amountButtonSelector: CustomButtonSelector
    private var daysTerms = 30;

    private var RpAmount = "0"

    // 1: Propuesta 2: Confirmación
    private var renovationStep = 1

    // Montos disponibles
    var montosDisponibles: List<MontoDisponible> = emptyList()

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private val viewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(
            UserRepository(),
            preferencesManager = userPreferencesManager
        )
    }
    private val loanViewModel: LoanRenewViewModel by activityViewModels {
        LoanRenewalViewModelFactory(
            LoanRepository(),
            preferencesManager = userPreferencesManager
        )
    }

    private var lastLoan: Prestamo? = null
    private var realTerm = 15

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userPreferencesManager = PreferencesManager(requireContext()) // Inicialización segura
        loanPreferencesManager = PreferencesManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRenewalProposalBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun checkAndRun() {
        if (loanType.isNotEmpty() && userData != null) {
            lifecycleScope.launch {
                Log.d("RenewalProposalFragment CR", "Cargando propuesta para $loanType")
                when(loanType){
                    "MINI" -> prestamoMini()
                    "RP" -> prestamoRP()
                    "PLP" -> return@launch
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextNeedHelp = view.findViewById(R.id.form_need_help)
        errorNeedHelp = view.findViewById(R.id.error_need_help)
        textViewNeedHelp = view.findViewById(R.id.form_need_help_title)

        editTextNeedHelp.setOnClickListener {
            showBottomSheetDialogNeedHelp()
        }

        // Botón carga archivo
        uploadDocumentContainer = view.findViewById(R.id.uploadContainerBorder)
        uploadDocumentContainerTitle = view.findViewById(R.id.uploadContainerTitle)
        amountButtonSelector = view.findViewById(R.id.customButtonSelector)

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        Log.d("RenewalProposalFragment", "Cargando propuesta")

        //repository = LoanRepository()
        loanRepository = LoanRepository()

        formatter.applyPattern("#,##0.00")  // Ensures two decimal places

        // Tipo préstamo
        renewalViewModel.loanType.observe(viewLifecycleOwner) { data ->
            data?.let {
                loanType = it
                Log.d("RenewalProposalFragment", "Tipo de préstamo: ${loanType}")
            }
        }

        renewalViewModel.formId.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                formId = data
                Log.e("RenewalProposalFragment", "Recibido formulario: $formId")
                // Validar si se cargaron lso datos
                checkAndRun()
            }
        }

        // Observar formulario paso 3
        renewalViewModel.plpStep3Request.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                plpSept3Data = data
                Log.d("RenewalProposalFragment", "plpSept3Data data: $plpSept3Data")
                // Validar si se cargaron los datos
                checkAndRun()
            }
        }

        viewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("RenewalProposalFragment", "Datos de usuario renovación: ${data}")
                userData = data

                if(data.prestamos?.isNotEmpty() == true){
                    lastLoan = data.prestamos.first()
                }

                // Validar si se cargaron los datos
                checkAndRun()
            }
        }

        // Check de tecnología
        binding.checkboxUserTerms.setOnClickListener(null)
        binding.checkboxUserTerms.setOnClickListener {

            val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
            val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

            val isChecked = binding.checkboxUserTerms.isChecked
            binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)

            if (isChecked) {
                technologyCheck = true
            } else {
                technologyCheck = false
                // Mostrar modal cargo de tecnología
                showcargoTecnology()
            }
            view.post {
                //setCreditData(loanValue, loanTerm)
                lifecycleScope.launch {
                    when(loanType){
                        "MINI" -> setCreditData(loanValue, loanTerm)
                        "RP" -> setCreditDataRp(loanValue, loanTerm)
                    }
                }
            }

            // Actualizar en modelo
            renewalViewModel.setTechnologyCheck(technologyCheck)
        }

        val deadLineSeekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.deadlineSeekbar)
        val deadLineText: TextView = binding.root.findViewById<TextView>(R.id.deadlineText)

        deadLineSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(deadLineSeekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualValue = progress + 1 // Offset by min value
                if(actualValue == 1) deadLineText.text = "$actualValue Cuota"
                else deadLineText.text = "$actualValue Cuotas"

                // Cálculo de valores de crédito
                loanTerm = actualValue
                daysTerms = actualValue * 15
                if (loanType.isNotEmpty() && userData != null){
                    when(loanType){
                        "MINI" -> setCreditData(loanValue, loanTerm)
                        "RP" -> setCreditDataRp(loanValue, loanTerm)
                    }
                }
                //println("Cuotas listener: $loanTerm")
            }

            override fun onStartTrackingTouch(deadLineSeekBar: SeekBar?) {}
            override fun onStopTrackingTouch(deadLineSeekBar: SeekBar?) {}
        }) // Fin listener deadline seekbar

        // simular evento para inicializar fechas
        view.post {
            //deadLineSeekBarChangeListener.onProgressChanged(deadLineSeekBar, 0, false)
            //setCreditData(loanValue, loanTerm)
        }

        val btnNext = view.findViewById<Button>(R.id.btnNextStepRenewal)
        btnNext.setOnClickListener {
            if(renovationStep == 1){
                // Cargar propuesta
                lifecycleScope.launch {
                    prestamoRPStep4()
                }

                //Mostrar botón de carga de archivo
                uploadDocumentContainer.visibility = View.VISIBLE
                uploadDocumentContainerTitle.visibility = View.VISIBLE
                ++renovationStep

                // Mostrar opciones de ayuda
                editTextNeedHelp.visibility = View.VISIBLE
                textViewNeedHelp.visibility = View.VISIBLE

            }else if(renovationStep == 2){
                // Validar datos
                lifecycleScope.launch {
                    when(loanType){
                        "MINI" -> submitMini()
                        "RP" -> submitRp()
                        "PLP" -> return@launch
                    }
                }
            }

        }

        recyclerView = binding.creditDetailLoanProposal.recyclerViewPaymentsProposal
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoanPaymentAdapter(paymentDates)
        recyclerView.adapter = adapter

        val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
        val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

        binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(checkedColor)

        binding.uploadContainer?.setOnClickListener {
            openFilePicker()
        }
        binding.cancelUpload?.setOnClickListener {
            resetUploadState()
        }

    }

    private fun setupButtonSelector(buttonValues: List<ButtonValue>) {

        // Formato de valores
        for (value in buttonValues) {
            val formattedValue = formatter.format(value.label.toDouble())
            value.label = "$currencySymbol$formattedValue"
        }

        val customSelector = view?.findViewById<CustomButtonSelector>(R.id.customButtonSelector)

        customSelector?.let { selector ->
            selector.setButtonValuesWithState(buttonValues, 0)
            selector.setOnSelectionChangedListener { position, value ->
                println("Seleccionado: $value en posición $position")
                // Actualizar valor en pantalla
                val loanValueTextView: TextView = binding.root.findViewById<TextView>(R.id.value_amount_proposal)
                val cleanValue = value.drop(1).replace(",", "")
                proposalValue = cleanValue.toDouble()
                val formattedValue = formatter.format(proposalValue)
                loanValueTextView.text = "$currencySymbol$formattedValue $countryCurrency"
                // Recargar calculadora
                loadDataRp()
            }
        }
    }

    fun validateBeforeSubmit(): Boolean {
        if (!fileSelected || base64File.isNullOrEmpty()) {
            // Always do validation
            Log.d("Validation", "❌ Archivo no seleccionado")

            // Only show UI error if it's safe
            if (isAdded && _binding != null) {
                setUploadStateError("Selecciona un archivo")
            } else {
                Log.w("Validation", "⚠️ Fragment not attached or binding null - can't show UI error")
            }

            return false
        }
        return true
    }

    private suspend fun submitMini(){

        // Validar archivo
        if (!validateBeforeSubmit()) return

        // Validar campo ayuda
        val needHelp = editTextNeedHelp

        errorNeedHelp.visibility = View.GONE

        if (needHelp.text.toString().isEmpty()) {
            errorNeedHelp.visibility = View.VISIBLE
            needHelp.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            needHelp.requestFocus()
            return
        } else {
            errorNeedHelp.visibility = View.GONE
            needHelp.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        if(loanRenewalResponse.id.isEmpty() || base64File.isNullOrEmpty() || base64FileName.isNullOrEmpty()
            || base64FileType.isNullOrEmpty() || loanRenewalResponse.plazo.isEmpty()){
            // Pantalla de rechazo
            gotoRejectScreen()
        }

//        val diasPlazo = loanRenewalResponse.plazo.trim().split(" ")[0].toIntOrNull()
        val diasPlazo = daysTerms

        val request = CompleteLoanMiniRenewalRequest(
            Id = loanRenewalResponse.id,
            checkTecnologia = binding.checkboxUserTerms.isChecked,
            plazo = diasPlazo!!,
            requirioAyuda = needHelp.text.toString(),
            ordenPatronalContentType = base64FileType.toString(),
            ordenPatronalName = base64FileName.toString(),
            ordenPatronal = base64File.toString()
        )
        Log.d("RenewalProposalFragment CR", "Datos a enviar: $request")
        //return

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val response = withContext(Dispatchers.IO) {
                loanRepository.completeLoanMiniRenewal(request)
            }
            Log.d("RenewalProposalFragment CR", "Datos response: ${response}")
            checkAndDismissDialog() // Ocultar dialog
            if (response != null) {
                if (response.estado == "Actualizado Correctamente") {
                    Log.d("RenewalProposalFragment CR", "OK")

                    // Avanza al siguiente paso
                    (requireParentFragment() as? RenewalFragment)?.goToNextStep()
                }else{
                    gotoRejectScreen()
                }
            }
        }catch (e: Exception) {
            Log.d("RenewalProposalFragment CR", "Error al enviar datos: $e")
            checkAndDismissDialog()
        }
    }

    private suspend fun submitRp(){

        // Validar archivo
        if (!validateBeforeSubmit()) return

        // Validar campo ayuda
        val needHelp = editTextNeedHelp

        errorNeedHelp.visibility = View.GONE

        if (needHelp.text.toString().isEmpty()) {
            errorNeedHelp.visibility = View.VISIBLE
            needHelp.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            needHelp.requestFocus()
            return
        } else {
            errorNeedHelp.visibility = View.GONE
            needHelp.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        if(loanRpRenewalResponse.id.isEmpty() || base64File.isNullOrEmpty() || base64FileName.isNullOrEmpty()
            || base64FileType.isNullOrEmpty() || loanRpRenewalResponse.plazo.isEmpty()){
            // Pantalla de rechazo
            gotoRejectScreen()
        }

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            val request = CompleteLoanRpRenewalRequest(
                Id = loanRpRenewalResponse.id,
                requirioAyuda = needHelp.text.toString(),
                ordenPatronalContentType = base64FileType.toString(),
                ordenPatronalName = base64FileName.toString(),
                ordenPatronal = base64File.toString()
            )

            Log.d("RenewalProposalFragment CR", "Datos a enviar: $request")
            //return

            try {
                val response = withContext(Dispatchers.IO) {
                    loanRepository.completeLoanRpRenewal(request)
                }
                Log.d("RenewalProposalFragment CR", "Datos response: ${response}")
                checkAndDismissDialog() // Ocultar dialog
                if (response != null) {
                    if (response.estado == "Actualizado Correctamente") {
                        Log.d("RenewalProposalFragment CR", "OK")

                        // Avanza al siguiente paso
                        (requireParentFragment() as? RenewalFragment)?.goToNextStep()
                    }else{
                        gotoRejectScreen()
                    }
                }
            }catch (e: Exception) {
                Log.d("RenewalProposalFragment CR", "Error al enviar datos: $e")
                checkAndDismissDialog()
            }
        } catch (e: Exception) {
            checkAndDismissDialog() // Ocultar en caso de error
            Log.e("RenewalProposalFragment", "Error obteniendo datos", e)
        }
    }

    private fun showcargoTecnology() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_cargo_tecnology, null)
        dialog.setContentView(dialogView)

        dialogView.findViewById<TextView>(R.id.form_link)?.setOnClickListener {
            abrirFormularioEnNavegador()
        }

        // Si necesitas interactuar con elementos del diálogo
        dialogView.findViewById<Button>(R.id.btn_close)?.setOnClickListener {
            dialog.dismiss()
            binding.checkboxUserTerms.isChecked = true
            val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
            binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(checkedColor)
        }

        dialogView.findViewById<MaterialTextView>(R.id.btn_close_days)?.setOnClickListener {
            dialog.dismiss()
            binding.checkboxUserTerms.isChecked = false
            val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
            binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(checkedColor)
            renewalViewModel.setTechnologyCheck(false)
        }

        dialog.show()
    }

    private fun abrirFormularioEnNavegador() {
        val url = "https://d2kkzfskpa3qm4.cloudfront.net/docs/FORMULARIO%20SOLICITUD%20CREDITO%20SIN%20CARGOS%20TECNOL%C3%93GICOS%20RAYO%20COL.pdf" // Reemplaza con tu URL real

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                // Fuerza a abrir en navegador externo en lugar de WebView interno
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Verifica si hay una app que pueda manejar la intent
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "No se encontró un navegador para abrir el enlace",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error al abrir el enlace: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("BottomSheet", "Error al abrir URL", e)
        }
    }

    private fun checkAndDismissDialog() {
        loadingDialog.dismiss()
    }

    fun String.toBooleanSI(): Boolean {
        return this.equals("SI", ignoreCase = true)
    }

    private fun gotoRejectScreen(){
        val bundle = Bundle().apply {
            putSerializable("outcome", LoanOutcome.REJECTED)
        }
        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
        return
    }

    private suspend fun prestamoMini(){
        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            val request = CreateLoanMiniRenewalRequest(
                userData?.id!!
            )
            // Guardar request
            //renewalViewModel.setProposalLoadRequestData(request)

            loanRenewalResponse = withContext(Dispatchers.IO) {
                loanRepository.createLoanMiniRenewal(request)!!
                ////loanViewModel.getData(request)
            }
            if(loanRenewalResponse != null){
                if(loanRenewalResponse.mensaje == "El cliente tiene prestamos pendientes por pagar"){
                    Log.d("RenewalProposalFragment CR", "No se puede continuar")
                    checkAndDismissDialog() // Ocultar dialog

                    // Pantalla de rechazo
                    gotoRejectScreen()
                }
                // Guardar response
                //renewalViewModel.setProposalLoadResponseData(loanRenewalResponse)

                //loanViewModel.setRenewalData(loanRenewalResponse)
                // asignar datos
                proposalValue = loanRenewalResponse.monto.toDouble()
                hasMora = "NO".toBooleanSI()

                // Asignar porcentaje de descuento
                //Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRenewalResponse.solicitud.porcentajeDescuento}")
                discountRateString = loanRenewalResponse.descuento
                renewalViewModel.setDiscountRate(loanRenewalResponse.descuento)

                // Guardar score experian
                scoreExperianString = "0"

                loadData()

                // Forzar la ejecución del listener al inicio
                binding.checkboxUserTerms.setOnCheckedChangeListener(null) // Remove previous listener
                binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Avoid weird animations
                binding.checkboxUserTerms.callOnClick() // Force execution of the listener

                checkAndDismissDialog() // Ocultar dialog
            }
            Log.d("RenewalProposalFragment", "Datos api: ${loanRenewalResponse}")
        } catch (e: Exception) {
            checkAndDismissDialog() // Ocultar en caso de error
            Log.e("RenewalProposalFragment", "Error obteniendo datos", e)
            // Mostrar errores
        }
    }

    // Cargar datos iniciales
    private fun prestamoRP(){
        if(userData != null){
            val propuesta = PropuestaPrestamo(
                salario = userData!!.salario.toDouble().toInt(),
                cantidadPrestamos = userData!!.cantidadPrestamosRPLS.toInt() + userData!!.CantidadPrestamosMiniPlus.toInt(),
                cantidadPrestamosRayoPlus = userData!!.cantidadPrestamosRPLS.toInt()
            )

            val montos = generarMontosDisponibles(propuesta, MONTOS)
            //Log.d("RenewalProposalFragment CR", "Montos: ${montos.size}")
            for (monto in montos) {
                //println("Monto: ${monto}")
                if(!monto.disabled) RpAmount = monto.amount.toString()
            }
            // Agregar a lista de valores
            //val values: List<ButtonValue> = montos.map { a ->
            val values: MutableList<ButtonValue> = montos.map { a ->
                ButtonValue(
                    label = a.amount.toString(),
                    disabled = a.disabled
                )
            }.toMutableList()

            //values.add(ButtonValue(label = "40000", disabled = false))
            setupButtonSelector(values)

        }else{
            Log.d("RenewalProposalFragment CR", "userData null")
            return
        }

        // asignar datos
        hasMora = "NO".toBooleanSI()
        // Guardar score experian
        scoreExperianString = "0"

        if( ::loanRpRenewalResponse.isInitialized){
            Log.d("RenewalProposalFragment CR", "Cargando datos de loanRpRenewalResponse: $loanRpRenewalResponse")
            proposalValue = loanRpRenewalResponse.monto.toDouble()

            // Asignar porcentaje de descuento
            //Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRpRenewalResponse.solicitud.porcentajeDescuento}")
            discountRateString = loanRpRenewalResponse.descuento
            renewalViewModel.setDiscountRate(discountRateString)
        }else{
            Log.d("RenewalProposalFragment CR", "Cargando datos por defecto")
            proposalValue = RpAmount.toDouble()
            discountRateString = "0"
            renewalViewModel.setDiscountRate(discountRateString)
        }

        loadDataRp()

        // Forzar la ejecución del listener al inicio
        binding.checkboxUserTerms.setOnCheckedChangeListener(null) // Remove previous listener
        binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Avoid weird animations
        binding.checkboxUserTerms.callOnClick() // Force execution of the listener
    }

    // Cargar confirmación de propuesta
    private suspend fun prestamoRPStep4(){
        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            val request = CreateLoanRpRenewalRequest(
                userData?.id!!,
                checkTecnologia = technologyCheck,
                plazo = "$realTerm días", //"30 días",
                montoSolicitado = RpAmount // montos solicitados
            )

            loanRpRenewalResponse = withContext(Dispatchers.IO) {
                loanRepository.createLoanRpRenewal(request)!!
            }
            if(loanRpRenewalResponse != null){
                if(loanRpRenewalResponse.mensaje != "Solicitud creada correctamente"){
                    Log.d("RenewalProposalFragment CR", "No se puede continuar")
                    checkAndDismissDialog() // Ocultar dialog

                    // Pantalla de rechazo
                    gotoRejectScreen()
                    return
                }

                checkAndDismissDialog() // Ocultar dialog

                // Ocultar selector de montos
                amountButtonSelector.visibility = View.GONE

                // Cargar nuevamente propuesta
                prestamoRP()

                /*// asignar datos
               proposalValue = loanRpRenewalResponse.monto.toDouble()
               hasMora = "NO".toBooleanSI()

               // Asignar porcentaje de descuento
               //Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRpRenewalResponse.solicitud.porcentajeDescuento}")
               discountRateString = loanRpRenewalResponse.descuento
               renewalViewModel.setDiscountRate(loanRpRenewalResponse.descuento)

               // Guardar score experian
               scoreExperianString = "0"

               loadDataRp()

               // Forzar la ejecución del listener al inicio
               binding.checkboxUserTerms.setOnCheckedChangeListener(null) // Remove previous listener
               binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Avoid weird animations
               binding.checkboxUserTerms.callOnClick() // Force execution of the listener*/
            }
            Log.d("RenewalProposalFragment", "Datos api: ${loanRpRenewalResponse}")
        } catch (e: Exception) {
            checkAndDismissDialog() // Ocultar en caso de error
            Log.e("RenewalProposalFragment", "Error obteniendo datos", e)
            // Mostrar errores
        }
    }

    /*private suspend fun prestamoPLP(){
        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            val request = CreateLoanMiniRenewalRequest(
                userData?.id!!
            )
            // Guardar request
            //renewalViewModel.setProposalLoadRequestData(request)

            loanRenewalResponse = withContext(Dispatchers.IO) {
                loanRepository.createLoanMiniRenewal(request)!!
                ////loanViewModel.getData(request)
            }
            if(loanRenewalResponse != null){
                if(loanRenewalResponse.mensaje == "El cliente tiene prestamos pendientes por pagar"){
                    Log.d("RenewalProposalFragment CR", "No se puede continuar")
                    checkAndDismissDialog() // Ocultar dialog

                    // Pantalla de rechazo
                    gotoRejectScreen()
                }
                // Guardar response
                //renewalViewModel.setProposalLoadResponseData(loanRenewalResponse)

                //loanViewModel.setRenewalData(loanRenewalResponse)
                // asignar datos
                proposalValue = loanRenewalResponse.monto.toDouble()
                hasMora = "NO".toBooleanSI()

                // Asignar porcentaje de descuento
                //Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRenewalResponse.solicitud.porcentajeDescuento}")
                discountRateString = loanRenewalResponse.descuento
                renewalViewModel.setDiscountRate(loanRenewalResponse.descuento)

                // Guardar score experian
                scoreExperianString = "0"

                loadData()

                // Forzar la ejecución del listener al inicio
                binding.checkboxUserTerms.setOnCheckedChangeListener(null) // Remove previous listener
                binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Avoid weird animations
                binding.checkboxUserTerms.callOnClick() // Force execution of the listener

                checkAndDismissDialog() // Ocultar dialog
            }
            Log.d("RenewalProposalFragment", "Datos api: ${loanRenewalResponse}")
        } catch (e: Exception) {
            checkAndDismissDialog() // Ocultar en caso de error
            Log.e("RenewalProposalFragment", "Error obteniendo datos", e)
            // Mostrar errores
        }
    }*/

    // Obtener plazo de pagos
    fun getLoanTerm(periodo: String): Int {
        val dias = periodo.trim().split(" ")[0].toIntOrNull()
        return if (dias != null && dias % 15 == 0) {
            dias / 15
        } else {
            -1
        }
    }

    private fun loadData(){

        // Cargar variables de crédito
        selectedCountry = CreditParameterManager.getSelectedCountry()
        creditParams = CreditParameterManager.getCreditParameters()!!
        creditInfo = CreditInformationManager.getCreditInformation()!!

        if (creditParams != null && creditInfo != null) {
            Log.d("RenewalProposalFragment CR", "Country: $selectedCountry - Data: $creditParams")
            Log.d("RenewalProposalFragment CR", "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo")
        } else {
            Log.e("RenewalProposalFragment CR", "No credit parameters found!")
        }

        val loanValueTextView: TextView = binding.root.findViewById<TextView>(R.id.value_amount_proposal)
        val proposalValue = proposalValue
        Log.d("RenewalProposalFragment CR", "Propuesta: $proposalValue")
        RpAmount = proposalValue.toString()
        val formattedValue = formatter.format(proposalValue?.toDouble())
        loanValueTextView.text = "$currencySymbol$formattedValue $countryCurrency"

        //
        // Variables globales para cálculo de crédito
        loanValue = proposalValue.toInt();
        loanTerm = getLoanTerm(loanRenewalResponse.plazo)
        if(loanTerm == -1) Log.d("RenewalProposalFragment CR", "Plazo inválido: $loanTerm")

        val deadLineSeekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.deadlineSeekbar)
        val deadLineText: TextView = binding.root.findViewById<TextView>(R.id.deadlineText)
        val seekbarDeadLineMinValue: TextView = binding.root.findViewById<TextView>(R.id.minValueDeadline)
        val seekbarDeadLineMaxValue: TextView = binding.root.findViewById<TextView>(R.id.maxValueDeadline)

        // Listener deadline seekbar
        var deadLineMinValue = 1
        var deadLinMaxValue = 1

        // Asignar valor plazo mínimo por defecto previo a poner foco sobre controles
        if(deadLineMinValue == 1) deadLineText.text = "$deadLineMinValue Cuota"
        else deadLineText.text = "$deadLineMinValue Cuotas"

        deadLineMinValue = 1
        deadLinMaxValue = creditParams!!.maxQuotes
        deadLineSeekBar.max = deadLinMaxValue - 1
        deadLineSeekBar.progress = 0 + loanTerm
        // Valores de cuotas min y max
        seekbarDeadLineMinValue.text = "$deadLineMinValue cuota"
        seekbarDeadLineMaxValue.text = "$deadLinMaxValue cuotas"

        setCreditData(loanValue, loanTerm)
    }

    private fun loadDataRp(){

        val deadLineLayout: LinearLayout = binding.root.findViewById<LinearLayout>(R.id.deadlineLayout)
        val deadLineSeekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.deadlineSeekbar)
        val deadLineText: TextView = binding.root.findViewById<TextView>(R.id.deadlineText)
        val seekbarDeadLineMinValue: TextView = binding.root.findViewById<TextView>(R.id.minValueDeadline)
        val seekbarDeadLineMaxValue: TextView = binding.root.findViewById<TextView>(R.id.maxValueDeadline)

        // Cargar variables de crédito
        selectedCountry = CreditParameterManager.getSelectedCountry()
        creditParams = CreditParameterManager.getCreditParameters()!!
        creditInfo = CreditInformationManager.getCreditInformation()!!

        if (creditParams != null && creditInfo != null) {
            Log.d("RenewalProposalFragment CR", "Country: $selectedCountry - Data: $creditParams")
            Log.d("RenewalProposalFragment CR", "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo")
        } else {
            Log.e("RenewalProposalFragment CR", "No credit parameters found!")
        }

        val loanValueTextView: TextView = binding.root.findViewById<TextView>(R.id.value_amount_proposal)
        val proposalValue = proposalValue
        Log.d("RenewalProposalFragment CR", "Propuesta: $proposalValue")
        val formattedValue = formatter.format(proposalValue?.toDouble())
        loanValueTextView.text = "$currencySymbol$formattedValue $countryCurrency"

        //
        // Variables globales para cálculo de crédito
        loanValue = proposalValue.toInt();

        if( ::loanRpRenewalResponse.isInitialized){
            loanTerm = getLoanTerm(loanRpRenewalResponse.plazo)
            // Ocultar layout seekbar
            deadLineLayout.visibility = View.GONE

        }else{
            loanTerm = getLoanTerm("30 días")
        }
        Log.d("RenewalProposalFragment CR", "loanTerm: $loanTerm")

        if(loanTerm == -1) Log.d("RenewalProposalFragment CR", "Plazo inválido: $loanTerm")

        // Listener deadline seekbar
        var deadLineMinValue = 1
        if(loanTerm > 0) deadLineMinValue = loanTerm // Asignar valor de cuotas retornado en propuesta
        var deadLinMaxValue = 1

        // Asignar valor plazo mínimo por defecto previo a poner foco sobre controles
        if(deadLineMinValue == 1) deadLineText.text = "$deadLineMinValue Cuota"
        else deadLineText.text = "$deadLineMinValue Cuotas"

        deadLineMinValue = 1
        deadLinMaxValue = creditParams!!.maxQuotes
        deadLineSeekBar.max = deadLinMaxValue - 1
        deadLineSeekBar.progress = 0 + loanTerm-1
        // Valores de cuotas min y max
        seekbarDeadLineMinValue.text = "$deadLineMinValue cuota"
        seekbarDeadLineMaxValue.text = "$deadLinMaxValue cuotas"

        setCreditDataRp(loanValue, loanTerm)
    }

    private fun setCreditData(creditValue: Int, term: Int){
        Log.d("RenewalProposalFragment CR", "Params: term=${term} loanTerm = $loanTerm creditValue=$creditValue")
        Log.d("RenewalProposalFragment CR", "Params CR: $creditParams")
        //return

        val _loanTermFactor = getLoanTerm(loanRenewalResponse.plazo) // plazo para calcular interestRate

        realTerm = loanTerm * 15
        val rate = loanRenewalResponse.interes.toDouble() / _loanTermFactor //creditParams?.interestRate
        val ivaRate = 0.13 // loanRenewalResponse.iva.toDouble() / _loanTermFactor //creditParams?.tax
        val tecnoRate = loanRenewalResponse.tecnologia.toDouble() / _loanTermFactor
        val tecnoRateNet = loanRenewalResponse.servicioFE.toDouble() / _loanTermFactor

        //val loanInterest = ceil(rate * loanTerm)
        val loanInterest = getInteres(creditValue.toDouble(), term, loanRenewalResponse.descuentoWOW.toDouble())

        // Aval
        val aval = getAval("mc", creditValue, loanRenewalResponse.cantidadPrestamos.toInt())

        //val tech = getTehValue("mc", creditValue, term, loanRenewalResponse.tipoTecnologia.toInt())
        // Obtener costo de tech
        //val techValue = ceil(tecnoRate * term) //getTecnValue(loanType, creditValue, realTerm, technologyCheck, userScore)
        val techValue = getTechValueRenovation(creditValue, term, loanRenewalResponse.cantidadPrestamos.toInt()) + aval
        val techDiscount = getDescuentoTecnologia("mc", creditValue, term, loanRenewalResponse.cantidadPrestamos.toInt())
        Log.d("RenewalProposalFragment CR", "Descuento: $techDiscount - tec = $techValue")
        var techIva = ceil((techValue - techDiscount) * ivaRate)
        //var techIva = ceil(tech * ivaRate)
        //val techValueNet = ceil(tecnoRateNet * term)
        val techValueNet = ceil((techValue - techDiscount).toDouble())

        // Validar si aplica porcentaje descuento
        var loanDiscount = 0.0
        loanDiscount = loanRenewalResponse.descuento.toDouble()
        /*if( tenDiscountString.toBooleanSI() ){
            loanDiscount = creditValue.toDouble() * (10.0 / 100.0)
        }else if( discountRateString.isNotEmpty() ){
            val discountRate = discountRateString.toDouble()
            loanDiscount = creditValue.toDouble() * (discountRate / 100.0)
        }
        Log.d("RenewalProposalFragment", "Descuento aplicado: ${loanDiscount}")
        Log.d("RenewalProposalFragment", "Descuento 10: $tenDiscountString")
        Log.d("RenewalProposalFragment", "Descuento porcentaje: $discountRateString")*/

        val scoreExperian = scoreExperianString.toDouble()
        var fianza = 0.0
        var fianzaAsignada = 0.06

        /*fianzaAsignada = when {
            scoreExperian in 450.0..549.99 -> if (!hasMora) 0.05 else 0.06
            scoreExperian in 550.0..699.99 -> if (!hasMora) 0.0422 else 0.05
            scoreExperian >= 700.0 -> if (!hasMora) 0.0 else 0.0422
            else -> fianzaAsignada
        }*/

        //fianza = ceil((creditValue * fianzaAsignada) * 1.19)

        // Asignar valores

        // interés
        val loanInterestField: TextView = binding.root.findViewById<TextView>(R.id.interest_value_proposal)
        val loanInterestformattedValue = formatter.format(loanInterest)
        loanInterestField.text = "$currencySymbol$loanInterestformattedValue"

        // tech
        val techField: TextView = binding.root.findViewById<TextView>(R.id.technology_value_proposal)
        val techFormattedValue = formatter.format(techValue)
        techField.text = "$currencySymbol$techFormattedValue"

        // Net tech
        val techNetField: TextView = binding.root.findViewById<TextView>(R.id.technology_net_value_proposal)
        val techNetFormattedValue = formatter.format(techValueNet)
        techNetField.text = "$currencySymbol$techNetFormattedValue"

        // iva tech
        val ivaTechField: TextView = binding.root.findViewById<TextView>(R.id.IVAtechnology_value_proposal)
        val ivaTechFormattedValue = formatter.format(techIva)
        ivaTechField.text = "$currencySymbol$ivaTechFormattedValue"

        // Fianza
        /*val fianzaField: TextView = binding.root.findViewById<TextView>(R.id.bond_value_proposal)
        val fianzaFormattedValue = formatter.format(fianza)
        fianzaField.text = "$currencySymbol$fianzaFormattedValue"*/

        // Subtotal
        val subtotal = creditValue + loanInterest
        val subtotalField: TextView = binding.root.findViewById<TextView>(R.id.subtotal_value_proposal)
        val subtotalFormattedValue = formatter.format(subtotal)
        subtotalField.text = "$currencySymbol$subtotalFormattedValue"

        // Total a pagar
        val total = subtotal + techValue + techIva - techDiscount // subtotal + techValue + techIva + fianza - loanDiscount
        val totalField: TextView = binding.root.findViewById<TextView>(R.id.total_to_pay_value_proposal)
        val totalFormattedValue = formatter.format(total)
        totalField.text = "$currencySymbol$totalFormattedValue"

        // Total a pagar 2
        val totalField2: TextView = binding.root.findViewById<TextView>(R.id.total_payment_value_proposal)
        val totalFormattedValue2 = formatter.format(total)
        totalField2.text = "$currencySymbol$totalFormattedValue2"

        // Total desembolso
        val disbursementField: TextView = binding.root.findViewById<TextView>(R.id.total_disbursement_value_proposal)
        val disbursementFormattedValue = formatter.format(loanValue)
        disbursementField.text = "$currencySymbol$disbursementFormattedValue"

        // Valor sugerido
        val suggestedValueField: TextView = binding.root.findViewById<TextView>(R.id.suggested_value_proposal)
        suggestedValueField.text = "$currencySymbol$disbursementFormattedValue"

        // Descuentos
        /*val discountField: TextView = binding.root.findViewById<TextView>(R.id.discount_value_proposal)
        // Mostrar campos
        val containerLayout = binding.root.findViewById<LinearLayout>(R.id.discountContainer)
        containerLayout.visibility = View.VISIBLE

        val discountFormattedValue = formatter.format(loanDiscount)
        discountField.text = "$currencySymbol$discountFormattedValue"*/

        // Calcular fechas de pagos
        val newPaymentDates = calculatePaymentDates(term, total, Date())
        /*for (payment in newPaymentDates) {
            println("Fecha: ${payment.date}, Monto: $currencySymbol${formatter.format(payment.amount)}")
        }*/

        // Asignar datos a recyclerview de pagos
        //adapter = LoanPaymentAdapter(newPaymentDates)
        paymentDates.clear()
        paymentDates.addAll(newPaymentDates)
        //recyclerView.adapter = adapter
        adapter.notifyDataSetChanged() // Refresh RecyclerView

        // Actualizar plazo
        renewalViewModel.setLoanTerm(realTerm)
    }

    private fun setCreditDataRp(creditValue: Int, term: Int){
        Log.d("RenewalProposalFragment CR", "Params: term=${term} loanTerm = $loanTerm creditValue=$creditValue")
        Log.d("RenewalProposalFragment CR", "Params CR: $creditParams")
        //return

        //val _loanTermFactor = getLoanTerm(loanRpRenewalResponse.plazo) // plazo para calcular interestRate
        //var _loanTermFactor = 0

        if( ::loanRpRenewalResponse.isInitialized){
            loanTerm = getLoanTerm(loanRpRenewalResponse.plazo)
        }else{
            //loanTerm = getLoanTerm("30 días")
            loanTerm = term
        }

        realTerm = loanTerm * 15
        val ivaRate = 0.13 // loanRenewalResponse.iva.toDouble() / _loanTermFactor //creditParams?.tax

        //val loanInterest = getInteresRp(creditValue)
        var loanInterest = 0.0
        if(userData?.descuentoWOW?.isNotEmpty() == true){
            val percentValue = userData!!.descuentoWOW.replace("%", "").toDouble()
            loanInterest = getInteres(creditValue.toDouble(), term, percentValue)
        }

        // Aval
        val aval = getAval("rp", creditValue, userData!!.numPrestamosFirmados.toInt())

        //val tech = getTehValue("mc", creditValue, term, loanRpRenewalResponse.tipoTecnologia.toInt())
        // Obtener costo de tech
        //val techValue = ceil(tecnoRate * term) //getTecnValue(loanType, creditValue, realTerm, technologyCheck, userScore)
        val techValue = getTechValueRenovation(creditValue, term, 5) + aval
        val techDiscount = getDescuentoTecnologia("rp", creditValue, term, 5)
        Log.d("RenewalProposalFragment CR", "Descuento: $techDiscount - tec = $techValue")
        //var techIva = getIVARp(aval, techValue-aval) // admin = aval ?
        var techIva = ceil((techValue - techDiscount) * ivaRate)
        //var techIva = ceil(tech * ivaRate)
        //val techValueNet = ceil(tecnoRateNet * term)
        val techValueNet = ceil((techValue - techDiscount).toDouble())

        val scoreExperian = scoreExperianString.toDouble()
        var fianza = 0.0
        var fianzaAsignada = 0.06

        // Asignar valores

        // interés
        val loanInterestField: TextView = binding.root.findViewById<TextView>(R.id.interest_value_proposal)
        val loanInterestformattedValue = formatter.format(loanInterest)
        loanInterestField.text = "$currencySymbol$loanInterestformattedValue"

        // tech
        val techField: TextView = binding.root.findViewById<TextView>(R.id.technology_value_proposal)
        val techFormattedValue = formatter.format(techValue)
        techField.text = "$currencySymbol$techFormattedValue"

        // Net tech
        val techNetField: TextView = binding.root.findViewById<TextView>(R.id.technology_net_value_proposal)
        val techNetFormattedValue = formatter.format(techValueNet)
        techNetField.text = "$currencySymbol$techNetFormattedValue"

        // iva tech
        val ivaTechField: TextView = binding.root.findViewById<TextView>(R.id.IVAtechnology_value_proposal)
        val ivaTechFormattedValue = formatter.format(techIva)
        ivaTechField.text = "$currencySymbol$ivaTechFormattedValue"

        // Fianza
        /*val fianzaField: TextView = binding.root.findViewById<TextView>(R.id.bond_value_proposal)
        val fianzaFormattedValue = formatter.format(fianza)
        fianzaField.text = "$currencySymbol$fianzaFormattedValue"*/

        // Subtotal
        val subtotal = creditValue + loanInterest
        val subtotalField: TextView = binding.root.findViewById<TextView>(R.id.subtotal_value_proposal)
        val subtotalFormattedValue = formatter.format(subtotal)
        subtotalField.text = "$currencySymbol$subtotalFormattedValue"

        // Total a pagar
        val total = subtotal + techValue + techIva - techDiscount // subtotal + techValue + techIva + fianza - loanDiscount
        val totalField: TextView = binding.root.findViewById<TextView>(R.id.total_to_pay_value_proposal)
        val totalFormattedValue = formatter.format(total)
        totalField.text = "$currencySymbol$totalFormattedValue"

        // Total a pagar 2
        val totalField2: TextView = binding.root.findViewById<TextView>(R.id.total_payment_value_proposal)
        val totalFormattedValue2 = formatter.format(total)
        totalField2.text = "$currencySymbol$totalFormattedValue2"

        // Total desembolso
        val disbursementField: TextView = binding.root.findViewById<TextView>(R.id.total_disbursement_value_proposal)
        val disbursementFormattedValue = formatter.format(loanValue)
        disbursementField.text = "$currencySymbol$disbursementFormattedValue"

        // Valor sugerido
        val suggestedValueField: TextView = binding.root.findViewById<TextView>(R.id.suggested_value_proposal)
        suggestedValueField.text = "$currencySymbol$disbursementFormattedValue"

        // Calcular fechas de pagos
        val newPaymentDates = calculatePaymentDates(term, total.toDouble(), Date())
        /*for (payment in newPaymentDates) {
            println("Fecha: ${payment.date}, Monto: $currencySymbol${formatter.format(payment.amount)}")
        }*/

        // Asignar datos a recyclerview de pagos
        //adapter = LoanPaymentAdapter(newPaymentDates)
        paymentDates.clear()
        paymentDates.addAll(newPaymentDates)
        //recyclerView.adapter = adapter
        adapter.notifyDataSetChanged() // Refresh RecyclerView

        // Actualizar plazo
        renewalViewModel.setLoanTerm(realTerm)
    }

    fun getInteres(valorSolicitado: Double, numeroCuotas: Int, descuentoWow: Double): Double {
        println("descuentoWow: $descuentoWow")
        //val pInteresBase = String.format("%.2f", valorSolicitado * 0.0125 * numeroCuotas).toDouble()
        val pInteresBase = (valorSolicitado * 0.0125 * numeroCuotas)
        val pInteres = pInteresBase * (1 - (descuentoWow / 100))

        return pInteres
    }

    fun getInteresRp(valorSolicitar: Int): Int {
        val pInteres = (valorSolicitar * 0.1).roundToInt()
        return pInteres
    }

    fun getAval(tipoPrestamo: String, valorSolicitado: Int, cantidadPrestamos: Int): Int {
        var pAval = 0.0
        if (tipoPrestamo == "mc") {
            pAval = if (cantidadPrestamos < 12) {
                valorSolicitado * 0.06
            } else {
                valorSolicitado * 0.08
            }
        } else if (tipoPrestamo == "rp") {
            pAval = if (cantidadPrestamos < 12) {
                valorSolicitado * 0.14
            } else {
                valorSolicitado * 0.16
            }
            pAval = round(pAval)
        }
        return pAval.toInt()
    }

    fun getIVARp(administracion: Int, tecnologia: Int): Int {
        val pIVA = (((administracion + tecnologia) * 19) / 100.0).roundToInt()
        return pIVA
    }

    fun getTehValue(
        tipoPrestamo: String,
        valorSolicitado: Int,
        numeroCuotas: Int,
        tipoTecnologia: Int
    ): Int {
        var pTecnologia = Math.round(valorSolicitado * 0.06).toInt()

        if (tipoPrestamo == "mc") {
            pTecnologia += when (tipoTecnologia) {
                1 -> when (valorSolicitado) {
                    20000 -> 15 * numeroCuotas * 269
                    25000 -> 15 * numeroCuotas * 430
                    30000, 35000 -> 15 * numeroCuotas * 457
                    40000 -> 15 * numeroCuotas * 538
                    50000 -> 15 * numeroCuotas * 591
                    75000 -> 15 * numeroCuotas * 860
                    100000 -> 15 * numeroCuotas * 1075
                    else -> 0
                }
                else -> when (valorSolicitado) {
                    20000 -> 15 * numeroCuotas * 250
                    25000 -> 15 * numeroCuotas * 400
                    30000, 35000 -> 15 * numeroCuotas * 425
                    40000 -> 15 * numeroCuotas * 500
                    50000 -> 15 * numeroCuotas * 550
                    75000 -> 15 * numeroCuotas * 800
                    100000 -> 15 * numeroCuotas * 1000
                    else -> 0
                }
            }
        } else if (tipoPrestamo == "rp") {
            pTecnologia = when {
                valorSolicitado == 30000 || valorSolicitado == 35000 -> 15 * numeroCuotas * 700
                valorSolicitado in 40000..55000 -> 15 * numeroCuotas * 900
                valorSolicitado in 60000..150000 -> 15 * numeroCuotas * 1100
                else -> pTecnologia
            }
        }

        return pTecnologia
    }


    fun getTechValueRenovation(valorSolicitado: Int, numeroCuotas: Int, cantidadPrestamos: Int): Int {
        val multiplicador = when {
            cantidadPrestamos < 3 -> when (valorSolicitado) {
                20000 -> 250
                25000 -> 400
                30000, 35000 -> 425
                40000 -> 500
                50000 -> 550
                75000 -> 800
                100000 -> 1000
                else -> 0
            }
            cantidadPrestamos in 3 until 12 -> when (valorSolicitado) {
                20000, 25000, 30000, 35000 -> 700
                40000, 50000 -> 900
                75000, 100000 -> 1100
                else -> 0
            }
            cantidadPrestamos >= 12 -> when (valorSolicitado) {
                20000, 25000, 30000, 35000 -> 700
                40000, 50000 -> 900
                75000, 100000 -> 1100
                else -> 0
            }
            else -> 0
        }

        return 15 * numeroCuotas * multiplicador
    }

    fun getDescuentoTecnologia(
        tipoPrestamo: String,
        valorSolicitado: Int,
        numeroCuotas: Int,
        cantidadPrestamos: Int
    ): Int {
        var pDescuento = 0

        if (tipoPrestamo == "mc") {
            return if (cantidadPrestamos == 2) {
                3000
            } else {
                calculosDescuento(valorSolicitado, cantidadPrestamos, numeroCuotas * 15)
            }
        } else if (tipoPrestamo == "rp") {
            val listMontos = DESCUENTOS_RP.firstOrNull { it.nCuotas == numeroCuotas }?.listMontos ?: return 0
            val listCantidadPrestamos = listMontos.firstOrNull { it.monto == valorSolicitado }?.listCantidadPrestamos ?: return 0
            pDescuento = listCantidadPrestamos.firstOrNull { it.cantidad <= cantidadPrestamos }?.descuento ?: 0
        }

        return pDescuento
    }

    fun calculosDescuento(monto: Int, cantidadPrestamos: Int, plazo: Int): Int {
        var descuento = 0
        if (cantidadPrestamos == 6) descuento = 4000

        return when (plazo) {
            15 -> when (monto) {
                20000 -> when {
                    cantidadPrestamos >= 12 -> 8920
                    cantidadPrestamos >= 3 -> 7636 + descuento
                    else -> 0
                }
                25000 -> when {
                    cantidadPrestamos >= 12 -> 7212
                    cantidadPrestamos >= 3 -> 5607 + descuento
                    else -> 0
                }
                30000 -> when {
                    cantidadPrestamos >= 12 -> 7379
                    cantidadPrestamos >= 3 -> 5453 + descuento
                    else -> 0
                }
                35000 -> when {
                    cantidadPrestamos >= 12 -> 7923
                    cantidadPrestamos >= 3 -> 5675 + descuento
                    else -> 0
                }
                40000 -> when {
                    cantidadPrestamos >= 12 -> 9965
                    cantidadPrestamos >= 3 -> 7395 + descuento
                    else -> 0
                }
                50000 -> when {
                    cantidadPrestamos >= 12 -> 10674
                    cantidadPrestamos >= 3 -> 7462 + descuento
                    else -> 0
                }
                75000 -> when {
                    cantidadPrestamos >= 12 -> 12637
                    cantidadPrestamos >= 3 -> 7819 + descuento
                    else -> 0
                }
                100000 -> when {
                    cantidadPrestamos >= 12 -> 12350
                    cantidadPrestamos >= 3 -> 5926 + descuento
                    else -> 0
                }
                else -> 0
            }

            30 -> when (monto) {
                20000 -> when {
                    cantidadPrestamos >= 12 -> 15670
                    cantidadPrestamos >= 3 -> 14385 + descuento
                    else -> 0
                }
                25000 -> when {
                    cantidadPrestamos >= 12 -> 11712
                    cantidadPrestamos >= 3 -> 10107 + descuento
                    else -> 0
                }
                30000 -> when {
                    cantidadPrestamos >= 12 -> 11505
                    cantidadPrestamos >= 3 -> 9580 + descuento
                    else -> 0
                }
                35000 -> when {
                    cantidadPrestamos >= 12 -> 12047
                    cantidadPrestamos >= 3 -> 9800 + descuento
                    else -> 0
                }
                40000 -> when {
                    cantidadPrestamos >= 12 -> 15590
                    cantidadPrestamos >= 3 -> 13020 + descuento
                    else -> 0
                }
                50000 -> when {
                    cantidadPrestamos >= 12 -> 15925
                    cantidadPrestamos >= 3 -> 12713 + descuento
                    else -> 0
                }
                75000 -> when {
                    cantidadPrestamos >= 12 -> 17137
                    cantidadPrestamos >= 3 -> 12318 + descuento
                    else -> 0
                }
                100000 -> when {
                    cantidadPrestamos >= 12 -> 13850
                    cantidadPrestamos >= 3 -> 7425 + descuento
                    else -> 0
                }
                else -> 0
            }

            45 -> when (monto) {
                20000 -> when {
                    cantidadPrestamos >= 12 -> 22420
                    cantidadPrestamos >= 3 -> 21137 + descuento
                    else -> 0
                }
                25000 -> when {
                    cantidadPrestamos >= 12 -> 16213
                    cantidadPrestamos >= 3 -> 14607 + descuento
                    else -> 0
                }
                30000 -> when {
                    cantidadPrestamos >= 12 -> 15630
                    cantidadPrestamos >= 3 -> 13702 + descuento
                    else -> 0
                }
                35000 -> when {
                    cantidadPrestamos >= 12 -> 16172
                    cantidadPrestamos >= 3 -> 13926 + descuento
                    else -> 0
                }
                40000 -> when {
                    cantidadPrestamos >= 12 -> 21215
                    cantidadPrestamos >= 3 -> 18645 + descuento
                    else -> 0
                }
                50000 -> when {
                    cantidadPrestamos >= 12 -> 21175
                    cantidadPrestamos >= 3 -> 17964 + descuento
                    else -> 0
                }
                75000 -> when {
                    cantidadPrestamos >= 12 -> 21637
                    cantidadPrestamos >= 3 -> 16819 + descuento
                    else -> 0
                }
                100000 -> when {
                    cantidadPrestamos >= 12 -> 15349
                    cantidadPrestamos >= 3 -> 8926 + descuento
                    else -> 0
                }
                else -> 0
            }

            else -> 0
        }
    }

    fun calculatePaymentDates(loanTerm: Int, feeAmount: Double, baseDate: Date): MutableList<PaymentDate> {
        val dateFormatter = SimpleDateFormat("d MMMM yyyy", Locale("es", "ES"))
        val calendar = Calendar.getInstance()
        calendar.time = baseDate

        val fees = mutableListOf<PaymentDate>()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val feeDate: Calendar = Calendar.getInstance()
        feeDate.time = baseDate

        when (currentDay) {
            in 1..6 -> {
                feeDate.set(Calendar.DAY_OF_MONTH, 15)
            }
            in 7..21 -> {
                feeDate.set(Calendar.DAY_OF_MONTH, feeDate.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            else -> {
                feeDate.add(Calendar.MONTH, 1)
                feeDate.set(Calendar.DAY_OF_MONTH, 15)
            }
        }

        val paymentAmount = feeAmount/loanTerm

        var formattedDate = dateFormatter.format(feeDate.time)
        // Capitalizar la primera letra del mes
        var capitalizedDate = formattedDate.replace(Regex("\\b([a-z])")) { matchResult ->
            matchResult.value.uppercase()
        }
        fees.add(PaymentDate(capitalizedDate, paymentAmount))

        for (i in 1 until loanTerm) {
            val feeDay = feeDate.get(Calendar.DAY_OF_MONTH)
            val lastDayOfCurrentMonth = feeDate.getActualMaximum(Calendar.DAY_OF_MONTH)

            if (feeDay == 15) {
                feeDate.set(Calendar.DAY_OF_MONTH, lastDayOfCurrentMonth)
            } else {
                feeDate.add(Calendar.MONTH, 1)
                feeDate.set(Calendar.DAY_OF_MONTH, 15)
            }

            formattedDate = dateFormatter.format(feeDate.time)
            // Capitalizar la primera letra del mes
            capitalizedDate = formattedDate.replace(Regex("\\b([a-z])")) { matchResult ->
                matchResult.value.uppercase()
            }
            fees.add(PaymentDate(capitalizedDate, paymentAmount))
        }

        return fees
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "application/pdf"))
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val fileUri: Uri? = data?.data
            fileUri?.let {
                fileSelected = true
                setUploadStateSelected(it)
                simulateFileUpload(it)
            }
        }
    }

    private fun setUploadStateSelected(fileUri: Uri) {
        val fileName = getFileName(fileUri)
        base64FileName = fileName
        val fileExtension = getFileExtension(fileUri) ?: "Desconocido"
        //base64FileType = fileExtension

        // MIME type (image/jpeg, application/pdf)
        val mimeType = requireContext().contentResolver.getType(fileUri) ?: "application/octet-stream"
        base64FileType = mimeType

        if (!isAdded || _binding == null) return

        _binding?.apply {
            uploadContainerBorder?.background  = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_solid_blue)
            uploadStatusText?.text = fileName
            uploadTextType?.text = ".$fileExtension"
            uploadStatusText?.visibility = View.VISIBLE
        }
    }

    private fun getFileExtension(fileUri: Uri): String? {
        return getFileName(fileUri)?.substringAfterLast('.', "")
    }


    private fun getFileName(fileUri: Uri): String {
        var fileName = "Archivo seleccionado"
        val cursor = requireContext().contentResolver.query(fileUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                if (!displayName.isNullOrEmpty()) {
                    fileName = displayName
                }
            }
        }
        return fileName
    }

    private fun simulateFileUpload(fileUri: Uri) {
        if (!isAdded || _binding == null) return // ensure fragment is attached

        val mimeType = requireContext().contentResolver.getType(fileUri)

        //binding.uploadIcon.setImageResource(R.drawable.ic_document_upload)
        //binding.cancelUpload.visibility = View.GONE
        // Simular una subida con un retraso de 3 segundos
        //binding.uploadStatusText.text = "Cargando..."

        if (mimeType == null || (!mimeType.startsWith("image/") && !mimeType.startsWith("application/pdf"))) {
            setUploadStateError("Formato no permitido")
            return
        }

        // UI update: start loading state
        _binding?.apply {
            uploadIcon?.setImageResource(R.drawable.ic_document_upload)
            uploadStatusText?.text = "Cargando..."
            cancelUpload?.visibility = View.GONE
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded || _binding == null) return@postDelayed
            val fileSize = getFileSizeInMB(fileUri) // Obtener el tamaño del archivo
            if (fileSize > fileSizeLimit) {
                setUploadStateError("Archivo demasiado grande")
            } else {
                base64File = uriToBase64(fileUri)
                Log.d("Upload", "Base64 encoded file: ${base64File?.take(100)}...")
                setUploadStateSuccess(getFileName(fileUri))
            }
        }, 3000)
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let {
                Base64.encodeToString(it, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setUploadStateSuccess(fileName: String) {
        if (!isAdded || _binding == null) return

        val maxLength = 20
        val truncatedFileName = if (fileName.length > maxLength) {
            "${fileName.take(maxLength)}..."
        } else {
            fileName
        }
        _binding?.apply {
            uploadContainerBorder?.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_solid_blue)
            uploadIcon?.setImageResource(R.drawable.ic_document_upload)
            uploadStatusText?.text = truncatedFileName
            cancelUpload?.visibility = View.VISIBLE
            cancelUpload?.setImageResource(R.drawable.ic_trash) // Habilitar ícono inicial
        }
    }

    private fun setUploadStateError(message: String) {
        if (!isAdded || _binding == null) return

        // Reiniciar flags de validación
        fileSelected = false
        base64File = null
        base64FileName = null
        base64FileType = null

        _binding?.apply {
            uploadContainerBorder?.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_solid_red)
            uploadIcon?.setImageResource(R.drawable.ic_fail_document_upload)
            uploadStatusText?.text = message
            cancelUpload?.visibility = View.VISIBLE
            cancelUpload?.setImageResource(R.drawable.ic_x)
        }
    }

    /*private fun getFileSize(fileUri: Uri): Long {
        val cursor = requireContext().contentResolver.query(fileUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }
        }
        return 0
    }*/
    fun getFileSizeInMB(uri: Uri): Long {
        val returnCursor = requireContext().contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            val sizeBytes = it.getLong(sizeIndex)
            return sizeBytes / (1024 * 1024) // Convert to MB
        }
        return -1
    }


    private fun resetUploadState() {
        binding.uploadContainerBorder?.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_dashed)
        binding.uploadIcon?.setImageResource(R.drawable.ic_document_upload)
        binding.uploadStatusText?.text = "Haz clic para cargar soporte"
        binding.uploadStatusText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_900))
        binding.uploadTextType?.text = "PNG, JPG, PDF hasta $fileSizeLimit"

        // Ocultar el ícono de eliminar
        binding.cancelUpload?.visibility = View.GONE

        // Reiniciar flags de validación
        fileSelected = false
        base64File = null
        base64FileName = null
        base64FileType = null
    }

    companion object {
        private const val PICK_FILE_REQUEST_CODE = 100
    }

    private fun showBottomSheetDialogNeedHelp() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_need_help, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewNeedHelp)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = NeedHelpAdapter { selectedType ->
            binding.formNeedHelp.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }

    /*private fun openInfoDialog(title: String, message: String) {
        val dialog = InfoDialogFragment.newInstance(title, message)
        dialog.show(parentFragmentManager, "InfoDialogFragment")
    }*/

    // Generar montos disponibles
    fun generarMontosDisponibles(
        propuesta: PropuestaPrestamo,
        montos: List<RangoMonto>
    ): List<MontoDisponible> {
        val rango = montos.firstOrNull { r ->
            propuesta.salario in r.minSalario..r.maxSalario &&
                    propuesta.cantidadPrestamos >= r.cantidadPrestamos
        } ?: return emptyList()

        return rango.listMontos.mapIndexed { index, monto ->
            val disabled = when (index) {
                0 -> propuesta.cantidadPrestamosRayoPlus < 0
                1 -> propuesta.cantidadPrestamosRayoPlus <= 0
                2 -> propuesta.cantidadPrestamosRayoPlus <= 1
                3 -> propuesta.cantidadPrestamosRayoPlus <= 2
                4 -> propuesta.cantidadPrestamosRayoPlus <= 3
                5 -> propuesta.cantidadPrestamosRayoPlus <= 4
                else -> true
            }

            monto.copy(disabled = disabled)
        }
    }
}

data class PropuestaPrestamo(
    val salario: Int,
    val cantidadPrestamos: Int,
    val cantidadPrestamosRayoPlus: Int
)

data class CantidadPrestamos(
    val cantidad: Int,
    val descuento: Int
)

data class Monto(
    val monto: Int,
    val listCantidadPrestamos: List<CantidadPrestamos>
)

data class Descuento(
    val nCuotas: Int,
    val listMontos: List<Monto>
)
