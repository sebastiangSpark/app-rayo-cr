package com.rayo.rayoxml.cr.ui.loan

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.rayo.rayoxml.CR.ui.dialogs.DialogConfirmLargeButton
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.adapters.LoanPaymentAdapter
import com.rayo.rayoxml.cr.models.PaymentDate
import com.rayo.rayoxml.cr.services.Loan.CompleteLoanRequest
import com.rayo.rayoxml.cr.services.Loan.LoanRepository
import com.rayo.rayoxml.databinding.FragmentLoanProposalBinding
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.ui.dialogs.InfoDialogFragment
import com.rayo.rayoxml.cr.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.cr.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.cr.ui.renewal.custombutton.CustomButtonSelector
import com.rayo.rayoxml.cr.viewModels.FormViewModel
import com.rayo.rayoxml.utils.CreditInformation
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.CreditParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.round

class LoanProposalFragment(private val userParamsViewModel: UserViewModel) : Fragment() {

    private var _binding: FragmentLoanProposalBinding? = null
    private val binding get() = _binding!!
    private var technologyCheck = false

    private val currencySymbol = "₡"
    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
    private val countryCurrency = "CRC"

    private val formViewModel: FormViewModel by activityViewModels()

    private var selectedCountry: String = ""
    private var creditParams: CreditParameters? = null
    private var creditInfo: CreditInformation? = null

    // Recyclerview de pagos
    private var paymentDates = mutableListOf<PaymentDate>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanPaymentAdapter

    // viewmodel de datos de usuario
    //private lateinit var validationViewModel: LoanValidationStepViewModel

    //private var formId: String? = null
    //private var scoreExperianString: String = "0"
    private var formId: String? = null
    private var loanProposalValue: Double? = null
    private var loanInterest: Double? = null
    private var loanTechValue: Double? = null
    private var loanDiscount: Double? = null
    private var loanAval: Double? = null
    private var loanIva: Double? = null
    private var loanTotalValue: Double? = null
    private var loanFeValue: Double? = null

    // Variables globales de simulador de crédito
    private var loanValue = 0;
    private var loanTerm = 1;

    private lateinit var loanRepository: LoanRepository

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private val SUCCESSFUL_FORM_RESULT: String = "Actualizado Correctamente"

    //private var userData: Solicitud? = null
    //private var personalData: LoanStepOneRequest? = null
    //private var bankData: LoanStepTwoRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanProposalBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        formatter.applyPattern("#,##0.00")

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        loanRepository = LoanRepository()

        formViewModel.loanProposal.observe(viewLifecycleOwner) { data ->
            if (data != null && data.mensaje == "Solicitud creada correctamente") {
                Log.d("LoanProposalFormFragment", "Datos de propuesta: ${data}")
                formId = data.id
                loanProposalValue = data.monto.toDouble()
                loanInterest = data.interes.toDouble()
                loanTechValue = data.tecnologia.toDouble()
                loanDiscount = data.descuento.toDouble()
                loanAval = data.aval.toDouble()
                loanIva = data.iva.toDouble()
                loanTotalValue = data.totalPagar.toDouble()
                loanFeValue = data.servicioFE.toDouble()

                loadData()
            }
        }

        binding.btnNextStep.setOnClickListener {

            // Mostrar dialog
            loadingDialog.show(parentFragmentManager, "LoadingDialog")
            try {
                val request = CompleteLoanRequest(
                    Id = formId!!,
                    checkTecnologia = technologyCheck,
                    back64 = null,
                    front64 = null
                )

                lifecycleScope.launch {
                    val completeLoanResponse = withContext(Dispatchers.IO) {
                        loanRepository.completeLoan(formViewModel.authToken.value!!, request)
                    }

                    loadingDialog.dismiss()

                    Log.d("LoanProposalFragment CR", "Datos api: ${completeLoanResponse}")
                    if (completeLoanResponse != null && completeLoanResponse.estado == SUCCESSFUL_FORM_RESULT) {
                        // Continuar
                        (parentFragment as? FormFragment)?.goToNextStep()
                    } else {
                        Log.d("LoanProposalFragment CR", "createLoanResponse null")
                        // Mostrar pantalla de error
                        val bundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, bundle)
                    }
                }
            } catch (e: Exception) {
                loadingDialog.dismiss() // Ocultar en caso de error
                Log.e("LoanProposalFragment CR", "Error obteniendo datos", e)
            }
        }

        binding.checkboxUserTerms.setOnClickListener {
            openTermsDialog()
        }

        // Recyclerview de pagos
        //recyclerView = binding.creditDetailLoanProposal.recyclerViewPayments
        recyclerView = binding.creditDetailLoanProposal.recyclerViewPaymentsProposal
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoanPaymentAdapter(paymentDates)
        recyclerView.adapter = adapter

        // Check de tecnología
        val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
        val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

        binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(checkedColor)

//        binding.checkboxUserTerms.setOnClickListener {
//            val isChecked = binding.checkboxUserTerms.isChecked
//            binding.checkboxUserTerms.buttonTintList =
//                ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
//
//            if (isChecked) {
//                technologyCheck = true
//            } else {
//                technologyCheck = false
//            }
//            view.post {
//                setCreditData(loanValue, loanTerm)
//            }
//        }

        /*  repository = LoanRepository()

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
          *//*userParamsViewModel.formId.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("FormFragment", "Formulario asignado: $data")
                formId = data
            }
        }*//*

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
        }*/

        /*
                // Forzar la ejecución del listener al inicio
                binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Para evitar animaciones raras
                binding.checkboxUserTerms.callOnClick() // Fuerza la ejecución del listener
        */

        /*        binding.uploadContainer?.setOnClickListener {
                    openFilePicker()
                }
                binding.cancelUpload?.setOnClickListener {
                    resetUploadState()
                }*/
        //setupButtonSelector()
    }

    /*private fun setupButtonSelector() {
        val buttonValues = listOf(
            ButtonValue(label = "₡30,000.00", disabled = false),
            ButtonValue(label = "₡35,000.00", disabled = true)
        )

        val customSelector = view?.findViewById<CustomButtonSelector>(R.id.customButtonSelector)

        customSelector?.let { selector ->
            selector.setButtonValuesWithState(buttonValues, 0)
            selector.setOnSelectionChangedListener { position, value ->
                println("Seleccionado: $value en posición $position")
            }
        }
    }*/

    private fun openTermsDialog() {
        if (!binding.checkboxUserTerms.isChecked) {
            val dialog = BottomSheetDialog(requireContext())
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_cargo_tecnology, null)
            dialog.setContentView(dialogView)

            dialogView.findViewById<TextView>(R.id.form_link)?.setOnClickListener {
                openFormBrowser()
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
                openConfirmActionDialog(
                    title = "Cargo de Tecnología\n",
                    message = "Recuerda que al no aceptar el cobro de tecnología, debes enviar la documentación para tramitar tu solicitud y esperar 15 días hábiles para tu desembolso.",
                    confirmText = "Quiero mi crédito ya",
                    cancelText = "Quiero esperar los 15 días"
                )
            }
            dialog.show()
        }   else {
            technologyCheck = true
            binding.checkboxUserTerms.buttonTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.Matisse_700))
            view?.post {
                setCreditData(loanValue, loanTerm)
            }
        }
    }

    private fun openInfoDialog(title: String, message: String) {
        val dialog = InfoDialogFragment.newInstance(title, message)
        dialog.show(parentFragmentManager, "InfoDialogFragment")
    }

    private fun openConfirmActionDialog(
        title: String,
        message: String,
        confirmText: String,
        cancelText: String
    ) {
        val dialog = DialogConfirmLargeButton.newInstance(title, message, confirmText, cancelText)

        dialog.setDialogButtonClickListener(object :
            DialogConfirmLargeButton.DialogButtonClickListener {
            override fun onConfirmClicked() {
                binding.checkboxUserTerms.isChecked = true
                val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
                binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(checkedColor)
            }

            override fun onCancelClicked() {
                binding.checkboxUserTerms.isChecked = false
                val checkedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)
                binding.checkboxUserTerms.buttonTintList = ColorStateList.valueOf(checkedColor)
            }
        })

        dialog.show(parentFragmentManager, "DialogConfirmLargeButton")
    }

    private fun openFormBrowser() {
        val url =
            "https://d2kkzfskpa3qm4.cloudfront.net/docs/FORMULARIO%20SOLICITUD%20CREDITO%20SIN%20CARGOS%20TECNOL%C3%93GICOS%20RAYO%20COL.pdf"

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addCategory(Intent.CATEGORY_BROWSABLE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Check if there is an app to handle the intent
            val packageManager = requireContext().packageManager
            val activities = packageManager.queryIntentActivities(intent, 0)
            if (activities.isNotEmpty()) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "No app found to open the PDF. Please install a browser or PDF viewer.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error opening URL: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("LoanProposalFragment", "Error opening URL", e)
        }
    }

    private fun loadData() {

        // Cargar variables de crédito
        selectedCountry = CreditParameterManager.getSelectedCountry()
        creditParams = CreditParameterManager.getCreditParameters()!!
        creditInfo = CreditInformationManager.getCreditInformation()!!

        if (creditParams != null && creditInfo != null) {
            Log.d("LoanFragment", "Country: $selectedCountry - Data: $creditParams")
            Log.d(
                "LoanFragment",
                "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo"
            )
        } else {
            Log.e("LoanFragment", "No credit parameters found!")
        }

        val loanValueTextView: TextView =
            binding.root.findViewById<TextView>(R.id.value_amount_proposal)
        val formattedValue = formatter.format(loanProposalValue?.toDouble())
        loanValueTextView.text = "$currencySymbol$formattedValue $countryCurrency"

        //
        // Variables globales para cálculo de crédito
        loanValue = loanProposalValue!!.toInt();
        loanTerm = 1;

        val deadLineSeekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.deadlineSeekbar)
        val deadLineText: TextView = binding.root.findViewById<TextView>(R.id.deadlineText)
        val seekbarDeadLineMinValue: TextView =
            binding.root.findViewById<TextView>(R.id.minValueDeadline)
        val seekbarDeadLineMaxValue: TextView =
            binding.root.findViewById<TextView>(R.id.maxValueDeadline)

        // Listener deadline seekbar
        var deadLineMinValue = 1
        var deadLinMaxValue = 1

        // Asignar valor plazo mínimo por defecto previo a poner foco sobre controles
        if (deadLineMinValue == 1) deadLineText.text = "$deadLineMinValue Cuota"
        else deadLineText.text = "$deadLineMinValue Cuotas"

        deadLineMinValue = 1
        deadLinMaxValue = creditParams!!.maxQuotes
        deadLineSeekBar.max = deadLinMaxValue - 1
        deadLineSeekBar.progress = 0
        // Valores de cuotas min y max
        seekbarDeadLineMinValue.text = "$deadLineMinValue cuota"
        seekbarDeadLineMaxValue.text = "$deadLinMaxValue cuotas"

        val deadLineSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                deadLineSeekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                val actualValue = progress + 1 // Offset by min value
                if (actualValue == 1) deadLineText.text = "$actualValue Cuota"
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
        /*val textViewImportant: TextView = binding.root.findViewById<TextView>(R.id.text_final_credit_detail)
        if (creditInfo != null) {
            textViewImportant.text = "Importante: ${creditInfo!!.important}"
        }*/

        // simular evento para inicializar fechas
        // seekBarChangeListener debe ejecutarse DESPUÉS de la inicialización
        view?.post {
            //deadLineSeekBarChangeListener.onProgressChanged(deadLineSeekBar, 0, false)
            setCreditData(loanValue, loanTerm)
        }
    }

    private fun setCreditData(creditValue: Int, term: Int) {
        val realTerm = term * 15
        val loanInterest = setInterestValue(loanProposalValue!!.toInt(), term)
        val techValue = setPTechValue(loanProposalValue!!.toInt(), term);
        val techIva = setIvaValue(techValue)

        // Asignar valores

        // Valor sugerido
        // Valor sugerido
        val suggestedValueField: TextView =
            binding.root.findViewById<TextView>(R.id.suggested_value_proposal)
        val loanSuggestedformattedValue = formatter.format(loanProposalValue)
        suggestedValueField.text = "$currencySymbol$loanSuggestedformattedValue"

        // interés
        val loanInterestField: TextView =
            binding.root.findViewById<TextView>(R.id.interest_value_proposal)
        val loanInterestformattedValue = formatter.format(loanInterest)
        loanInterestField.text = "$currencySymbol$loanInterestformattedValue"

        // Subtotal
        val subtotal = loanProposalValue!!.toInt() + loanInterest
        val subtotalField: TextView =
            binding.root.findViewById<TextView>(R.id.subtotal_value_proposal)
        val subtotalFormattedValue = formatter.format(subtotal)
        subtotalField.text = "$currencySymbol$subtotalFormattedValue"

        // tech
        val techField: TextView =
            binding.root.findViewById<TextView>(R.id.technology_value_proposal)
        val techFormattedValue = formatter.format(techValue)
        techField.text = "$currencySymbol$techFormattedValue"

        // Net tech
        val netTechField: TextView =
            binding.root.findViewById<TextView>(R.id.technology_net_value_proposal)
        val netTechFormattedValue = formatter.format(techValue)
        netTechField.text = "$currencySymbol$netTechFormattedValue"

        // iva
        val ivaTechField: TextView =
            binding.root.findViewById<TextView>(R.id.IVAtechnology_value_proposal)
        val ivaTechFormattedValue = formatter.format(techIva)
        ivaTechField.text = "$currencySymbol$ivaTechFormattedValue"

        // Total a pagar
        val total = subtotal + techValue + techIva
        val totalField: TextView =
            binding.root.findViewById<TextView>(R.id.total_to_pay_value_proposal)
        val totalFormattedValue = formatter.format(total)
        totalField.text = "$currencySymbol$totalFormattedValue"

        /*// Total a pagar 2
        val total = subtotal + techValue + techIva + fianza
        val totalField: TextView = binding.root.findViewById<TextView>(R.id.total_payment_value_proposal)
        val totalFormattedValue = formatter.format(total)
        totalField.text = "$currencySymbol$totalFormattedValue"
        // Total desembolso
        val disbursementField: TextView = binding.root.findViewById<TextView>(R.id.total_disbursement_value_proposal)
        val disbursementFormattedValue = formatter.format(loanValue)
        disbursementField.text = "$currencySymbol$disbursementFormattedValue"*/

        // Calcular fechas de pagos
        val newPaymentDates =
            calculatePaymentDates(term, loanTotalValue.toString().toDouble(), Date())

        // Asignar datos a recyclerview de pagos
        //adapter = LoanPaymentAdapter(newPaymentDates)
        paymentDates.clear()
        paymentDates.addAll(newPaymentDates)
        //recyclerView.adapter = adapter
        adapter.notifyDataSetChanged() // Refresh RecyclerView

        // Actualizar plazo
        userParamsViewModel.updateLoanTerm(realTerm)
    }

    private fun setInterestValue(creditValue: Int, installments: Int): Double {
        val interestRate = creditParams?.interestRate ?: 0.0125
        val pInterest = creditValue * interestRate * installments
        return pInterest
    }

    private fun setIvaValue(pTech: Double): Double {
        val ivaRate = creditParams?.tax ?: 0.13
        val pIva = pTech * ivaRate
        return pIva
    }

    private fun setPTechValue(creditValue: Int, installments: Int): Double {
        val techRate = creditParams?.tecnology ?: 0.06
        val constantFix = 15
        var creditValueModifier = 1
        when (creditValue) {
            20000 -> creditValueModifier = 269
            25000 -> creditValueModifier = 430
            30000, 35000 -> creditValueModifier = 457
            40000 -> creditValueModifier = 538
            50000 -> creditValueModifier = 591
            75000 -> creditValueModifier = 860
            100000 -> creditValueModifier = 1075
        }
        var pTech = round(creditValue * techRate)
        pTech += (constantFix * installments * creditValueModifier)
        return pTech
    }

    fun calculatePaymentDates(
        loanTerm: Int,
        feeAmount: Double,
        baseDate: Date
    ): MutableList<PaymentDate> {
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

        val paymentAmount = feeAmount / loanTerm

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

    /*private suspend fun runValidations(){
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
    }*/

    /*    private fun openFilePicker() {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = ""
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "application/pdf"))
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        }*/

    /*  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
      }*/


    /*    private fun getFileName(fileUri: Uri): String {
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
        }*/

    /*    private fun resetUploadState() {
            binding.uploadContainerBorder.background = ContextCompat.getDrawable(requireContext(), R.drawable.card_border_dashed)
            binding.uploadIcon.setImageResource(R.drawable.ic_document_upload)
            binding.uploadStatusText.text = "Haz clic para cargar soporte"
            binding.uploadStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_900))
            binding.uploadTextType.text = "PNG, JPG, PDF hasta 20MB"

            // Ocultar el ícono de eliminar
            binding.cancelUpload.visibility = View.GONE
        }*/

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