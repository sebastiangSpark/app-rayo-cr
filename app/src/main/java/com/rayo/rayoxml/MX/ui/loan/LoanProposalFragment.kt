package com.rayo.rayoxml.mx.ui.loan

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.LoanPaymentAdapter
import com.rayo.rayoxml.databinding.FragmentLoanProposalBinding
import com.rayo.rayoxml.mx.models.PaymentDate
import com.rayo.rayoxml.mx.services.Loan.Formulario
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepFourLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepFourSubmitRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModelFactory
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.ui.dialogs.InfoDialogFragment
import com.rayo.rayoxml.utils.CreditInformation
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.CreditParameters
import com.rayo.rayoxml.utils.PreferencesManager
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

class LoanProposalFragment(private val userParamsViewModel: UserViewModel) : Fragment() {

    private var _binding: FragmentLoanProposalBinding? = null
    private val binding get() = _binding!!
    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
    private val countryCurrency = "COP"

    private var selectedCountry: String = ""
    private var creditParams: CreditParameters? = null
    private var creditInfo: CreditInformation? = null

    // Recyclerview de pagos
    private var paymentDates = mutableListOf<PaymentDate>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanPaymentAdapter

    // viewmodel de datos de usuario
    private lateinit var validationViewModel: LoanValidationStepViewModel

    private var formId: String? = null
    private var scoreExperianString: String = "0"

    // Check de costo de tecnología
    private var technologyCheck = false

    // Variables globales de simulador de crédito
    private var loanValue = 0;
    private var loanTerm = 1;

    // Datos api
    private lateinit var repository: LoanRepository

    private val SUCCESSFUL_FORM_RESULT: String = "Formulario OK"

    //private var userData: Solicitud? = null
    private var personalData: LoanStepOneRequest? = null
    private var bankData: LoanStepTwoRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanProposalBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Alerts informativos
        val btnInfoInterest: ImageView = binding.root.findViewById(R.id.btn_info_interest_proposal)
        val btnInfoTecnology: ImageView = binding.root.findViewById(R.id.btn_info_tecnology_proposal)
        val btnInfoIVA: ImageView = binding.root.findViewById(R.id.btn_info_IVA_proposal)

        btnInfoInterest.setOnClickListener {
            openInfoDialog(getString(R.string.interest_info_title), getString(R.string.interest_info_content))
        }
        btnInfoTecnology.setOnClickListener {
            openInfoDialog(getString(R.string.technology_info_title), getString(R.string.technology_info_content))
        }
        btnInfoIVA.setOnClickListener {
            openInfoDialog(getString(R.string.iva_info_title), getString(R.string.iva_info_content))
        }

        repository = LoanRepository()

        formatter.applyPattern("#,##0.00")  // Ensures two decimal places

        // Obtener ViewModel compartido con la actividad
        val repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanValidationStepViewModelFactory(repository, preferencesManager)
        validationViewModel = ViewModelProvider(requireActivity(), factory)[LoanValidationStepViewModel::class.java]

        validationViewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.codigo == "200") {
                formId = data.formulario
                scoreExperianString = data.scoreExperian!!
                Log.d("LoanProposalFormFragment", "scoreExperian ${scoreExperianString}")
            }
        }

        // Observers para datos de formularios de datos
        /*userParamsViewModel.formId.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FormFragment", "Formulario asignado: $data")
                formId = data
            }
        }*/

        userParamsViewModel.personalData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FormFragment", "Datos personales: ${data}")
                personalData = data
            }
        }
        userParamsViewModel.bankdData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FormFragment", "Datos bancarios: ${data}")
                bankData = data
            }
        }

        binding.btnNextStep.setOnClickListener {

            (parentFragment as? FormFragment)?.goToNextStep(shouldUpdateStepper = false)

            //(parentFragment as? FormFragment)?.goToNextStep()
            // Validaciones
            lifecycleScope.launch{
                runValidations()
            }
        }

        // Check de tecnología
        binding.checkboxUserTerms.setOnClickListener {

            val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
            val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

            val isChecked = binding.checkboxUserTerms.isChecked
            binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)

            if (isChecked) {
                technologyCheck = true
            } else {
                technologyCheck = false
            }
            view.post {
                setCreditData(loanValue, loanTerm)
            }
        }

        // Forzar la ejecución del listener al inicio
        binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Para evitar animaciones raras
        binding.checkboxUserTerms.callOnClick() // Fuerza la ejecución del listener

        loadData()

        // Recyclerview de pagos
        //recyclerView = binding.creditDetailLoanProposal.recyclerViewPayments
        recyclerView = binding.creditDetailLoanProposal.recyclerViewPaymentsProposal
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoanPaymentAdapter(paymentDates)
        recyclerView.adapter = adapter

        binding.uploadContainer?.setOnClickListener {
            openFilePicker()
        }
        binding.cancelUpload?.setOnClickListener {
            resetUploadState()
        }

    }

    private fun openInfoDialog(title: String, message: String) {
        val dialog = InfoDialogFragment.newInstance(title, message)
        dialog.show(parentFragmentManager, "InfoDialogFragment")
    }

    private fun loadData(){

        // Cargar variables de crédito
        selectedCountry = CreditParameterManager.getSelectedCountry()
        creditParams = CreditParameterManager.getCreditParameters()!!
        creditInfo = CreditInformationManager.getCreditInformation()!!

        if (creditParams != null && creditInfo != null) {
            Log.d("LoanFragment", "Country: $selectedCountry - Data: $creditParams")
            Log.d("LoanFragment", "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo")
        } else {
            Log.e("LoanFragment", "No credit parameters found!")
        }

        val loanValueTextView: TextView = binding.root.findViewById<TextView>(R.id.value_amount_proposal)
        val proposalValue = (parentFragment as? FormFragment)?.getProposalValue()
        Log.d("LoanProposalFragment", "Propuesta: $proposalValue")
        val formattedValue = formatter.format(proposalValue?.toDouble())
        loanValueTextView.text = "$$formattedValue $countryCurrency"

        //
        // Variables globales para cálculo de crédito
        //var loanValue = proposalValue!!.toInt();
        //var loanTerm = 1;
        loanValue = proposalValue!!.toInt();
        loanTerm = 1;

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
        deadLineSeekBar.progress = 0
        // Valores de cuotas min y max
        seekbarDeadLineMinValue.text = "$deadLineMinValue cuota"
        seekbarDeadLineMaxValue.text = "$deadLinMaxValue cuotas"

        //deadLineSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        val deadLineSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(deadLineSeekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualValue = progress + 1 // Offset by min value
                if(actualValue == 1) deadLineText.text = "$actualValue Cuota"
                else deadLineText.text = "$actualValue Cuotas"

                // Cálculo de valores de crédito
                loanTerm = actualValue
                setCreditData(loanValue, loanTerm)
            }

            override fun onStartTrackingTouch(deadLineSeekBar: SeekBar?) {}
            override fun onStopTrackingTouch(deadLineSeekBar: SeekBar?) {}
        }
        // Fin listener deadline seekbar

        // Asociar listener al SeekBar
        deadLineSeekBar.setOnSeekBarChangeListener(deadLineSeekBarChangeListener)

        // Datos CreditInformation
        val textViewImportant: TextView = binding.root.findViewById<TextView>(R.id.text_final_credit_detail)
        if (creditInfo != null) {
            textViewImportant.text = "Importante: ${creditInfo!!.important}"
        }

        // simular evento para inicializar fechas
        // seekBarChangeListener debe ejecutarse DESPUÉS de la inicialización
        view?.post {
            //deadLineSeekBarChangeListener.onProgressChanged(deadLineSeekBar, 0, false)
            setCreditData(loanValue, loanTerm)
        }
    }

    private fun setCreditData(creditValue: Int, term: Int){
        val realTerm = term * 15
        val rate = creditParams?.interestRate //0.00064879
        val techRate = 1100
        val techRateTwo = 0.125
        val adminRate = 0.125
        val ivaRate = creditParams?.tax //19
        var loanInterest = ceil((creditValue * rate!!) * realTerm)
        var techValue = ceil((realTerm * techRate) + (creditValue * techRateTwo))
        // Actualizar costo tecnología
        if( !technologyCheck ){
            techValue = 0.0
        }
        //var adminValue = round(creditValue * adminRate)
        var adminValue = 0 // Ya está incluido en la fórmula de te techValue
        var techIva = ceil(((adminValue + techValue) * ivaRate!!) / 100)
        //Log.d("LoanFragment", "adminValue: $adminValue - techValue: $techValue")

        val scoreExperian = scoreExperianString.toDouble()
        val hasMora = false
        var fianza = 0

        if (scoreExperian <= 549) {
            fianza = if (!hasMora){
                ceil((creditValue * 0.05) * 1.19).toInt()
            } else ceil((creditValue * 0.06) * 1.19).toInt()
        }
        else if (550 <= scoreExperian && scoreExperian <= 699) {
            fianza = if (!hasMora){
                ceil((creditValue * 0.0422) * 1.19).toInt()
            } else ceil((creditValue * 0.05) * 1.19).toInt()
        } else if (scoreExperian >= 700) {
            fianza = if (!hasMora) 0;
            else ceil((creditValue * 0.05) * 1.19).toInt()
        }

        // Asignar valores

        // interés
        val loanInterestField: TextView = binding.root.findViewById<TextView>(R.id.interest_value_proposal)
        val loanInterestformattedValue = formatter.format(loanInterest)
        loanInterestField.text = "$$loanInterestformattedValue"

        // tech
        val techField: TextView = binding.root.findViewById<TextView>(R.id.technology_value_proposal)
        val techFormattedValue = formatter.format(techValue)
        techField.text = "$$techFormattedValue"

        // iva tech
        val ivaTechField: TextView = binding.root.findViewById<TextView>(R.id.IVAtechnology_value_proposal)
        val ivaTechFormattedValue = formatter.format(techIva)
        ivaTechField.text = "$$ivaTechFormattedValue"

        // Fianza
        val fianzaField: TextView = binding.root.findViewById<TextView>(R.id.bond_value_proposal)
        val fianzaFormattedValue = formatter.format(fianza)
        fianzaField.text = "$$fianzaFormattedValue"

        // Subtotal
        val subtotal = creditValue + loanInterest
        val subtotalField: TextView = binding.root.findViewById<TextView>(R.id.subtotal_value_proposal)
        val subtotalFormattedValue = formatter.format(subtotal)
        subtotalField.text = "$$subtotalFormattedValue"

        // Total a pagar
        val total = subtotal + techValue + techIva + fianza
        val totalField: TextView = binding.root.findViewById<TextView>(R.id.total_to_pay_value_proposal)
        val totalFormattedValue = formatter.format(total)
        totalField.text = "$$totalFormattedValue"

        // Total a pagar 2
        val totalField2: TextView = binding.root.findViewById<TextView>(R.id.total_payment_value_proposal)
        val totalFormattedValue2 = formatter.format(total)
        totalField2.text = "$$totalFormattedValue2"

        // Total desembolso
        val disbursementField: TextView = binding.root.findViewById<TextView>(R.id.total_disbursement_value_proposal)
        val disbursementFormattedValue = formatter.format(loanValue)
        disbursementField.text = "$$disbursementFormattedValue"

        // Valor sugerido
        val suggestedValueField: TextView = binding.root.findViewById<TextView>(R.id.suggested_value_proposal)
        suggestedValueField.text = "$$disbursementFormattedValue"

        // Calcular fechas de pagos
        val newPaymentDates = calculatePaymentDates(term, total, Date())
        /*for (payment in paymentDates) {
            println("Fecha: ${payment.date}, Monto: $${formatter.format(payment.amount)}")
        }*/

        // Asignar datos a recyclerview de pagos
        //adapter = LoanPaymentAdapter(newPaymentDates)
        paymentDates.clear()
        paymentDates.addAll(newPaymentDates)
        //recyclerView.adapter = adapter
        adapter.notifyDataSetChanged() // Refresh RecyclerView

        // Actualizar plazo
        userParamsViewModel.updateLoanTerm(realTerm)
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

    private suspend fun runValidations(){
        Log.d("FormFragment", "Formulario paso 4: $formId")
        // Actualziar datos requeridos paso 5
        userParamsViewModel.setFormId(formId!!)

        if(formId != null){
            val formData = LoanStepFourLoadRequest(formId!!)

            val loanStepFourLoadResponse = withContext(Dispatchers.IO) {
                repository.getDataStepFourLoad(formData)
            }

            Log.d("LoanProposalFragment", "Loan step four: ${loanStepFourLoadResponse}")

            if (loanStepFourLoadResponse != null){
                if(loanStepFourLoadResponse.formulario.codigo == "200"){

                    val data: Formulario = loanStepFourLoadResponse.formulario

                    if(data.result == SUCCESSFUL_FORM_RESULT && data.formulario?.isNotEmpty() == true){
                        // STEP 4
                        if(bankData != null){
                            val formData = LoanStepFourSubmitRequest(
                                formulario = data.formulario,
                                checkTecnologia = bankData!!.checkTecnologia,
                                banco = bankData!!.banco,
                                ciudadDepartamento = bankData!!.ciudadDepartamento,
                                departamento = bankData!!.departamento,
                                direccionExacta = bankData!!.direccionExacta,
                                contadorActualizado = 3,
                                plazoSeleccionado = bankData!!.plazoSeleccionado,
                                referenciaBancaria = bankData!!.referenciaBancaria,
                                referenciaBancaria2 = bankData!!.referenciaBancaria2,
                                telefonoEmpresa = bankData!!.telefonoEmpresa,
                                tipoCuenta = bankData!!.tipoCuenta,
                                showModal = bankData!!.showModal,
                                debito = true,
                                archivo = null // Adjuntar
                            )
                            val loanStepFourSubmitResponse = withContext(Dispatchers.IO) {
                                repository.getDataStepFourSubmit(formData)
                            }
                            if (loanStepFourSubmitResponse != null) {
                                if(loanStepFourSubmitResponse.solicitud.codigo == "200"){

                                    Log.d("FormFragment", "Actualizando plazo")
                                    userParamsViewModel.updateLoanTerm(bankData!!.plazoSeleccionado)

                                    // Continuar
                                    (parentFragment as? FormFragment)?.goToNextStep()

                                }else{
                                    // Error
                                    Log.d("LoanProposalFragment", "Error STEP4 Submit: ${loanStepFourSubmitResponse}")
                                }
                            }
                        }else{
                            Log.d("LoanProposalFragment", "User null data")
                        }
                    }else{
                        // Error
                        Log.d("LoanProposalFragment", "Error STEP4 Request: ${data.result}")
                    }

                }else{
                    // Error
                    Log.d("LoanProposalFragment", "Error STEP4 Request: ${loanStepFourLoadResponse.formulario.result}")
                }
            }
        }
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
                setUploadStateSelected(it)
                simulateFileUpload(it)
            }
        }
    }

    private fun setUploadStateSelected(fileUri: Uri) {
        val fileName = getFileName(fileUri)
        val fileExtension = getFileExtension(fileUri) ?: "Desconocido"

        binding.uploadContainerBorder.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_solid_blue)
        binding.uploadStatusText.text = fileName
        binding.uploadTextType.text = ".$fileExtension"
        binding.uploadStatusText.visibility = View.VISIBLE
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
        binding.uploadIcon.setImageResource(R.drawable.ic_document_upload)
        val mimeType = requireContext().contentResolver.getType(fileUri)

        binding.cancelUpload.visibility = View.GONE
        // Simular una subida con un retraso de 3 segundos
        binding.uploadStatusText.text = "Cargando..."

        if (mimeType == null || (!mimeType.startsWith("image/") && !mimeType.startsWith("application/pdf"))) {
            setUploadStateError("Formato no permitido")
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({
            val fileSize = getFileSize(fileUri) // Obtener el tamaño del archivo
            if (fileSize > 20 * 1024 * 1024) {
                setUploadStateError("Cargando...")
            } else {
                setUploadStateSuccess(getFileName(fileUri))
            }
        }, 3000)
    }

    private fun setUploadStateSuccess(fileName: String) {
        val maxLength = 20
        val truncatedFileName = if (fileName.length > maxLength) {
            "${fileName.take(maxLength)}..."
        } else {
            fileName
        }
        binding.uploadContainerBorder.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_solid_blue)
        binding.uploadIcon.setImageResource(R.drawable.ic_document_upload)
        binding.uploadStatusText.text = truncatedFileName
        binding.cancelUpload.visibility = View.VISIBLE
    }

    private fun setUploadStateError(message: String) {
        binding.uploadContainerBorder.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_solid_red)
        binding.uploadIcon.setImageResource(R.drawable.ic_fail_document_upload)
        binding.uploadStatusText.text = message
        binding.cancelUpload.visibility = View.VISIBLE
        binding.cancelUpload.setImageResource(R.drawable.ic_x)
    }

    private fun getFileSize(fileUri: Uri): Long {
        val cursor = requireContext().contentResolver.query(fileUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }
        }
        return 0
    }

    private fun resetUploadState() {
        binding.uploadContainerBorder.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_dashed)
        binding.uploadIcon.setImageResource(R.drawable.ic_document_upload)
        binding.uploadStatusText.text = "Haz clic para cargar soporte"
        binding.uploadStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_900))
        binding.uploadTextType.text = "PNG, JPG, PDF hasta 20MB"

        // Ocultar el ícono de eliminar
        binding.cancelUpload.visibility = View.GONE
    }

/*
    private fun openInfoDialog(message: String) {
        val dialog = InfoDialogFragment.newInstance(message)
        dialog.show(parentFragmentManager, "InfoDialogFragment")
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_FILE_REQUEST_CODE = 100
    }
}