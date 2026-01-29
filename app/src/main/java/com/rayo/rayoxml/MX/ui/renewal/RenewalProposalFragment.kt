package com.rayo.rayoxml.mx.ui.renewal

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.LoanPaymentAdapter
import com.rayo.rayoxml.databinding.FragmentRenewalProposalBinding

import com.rayo.rayoxml.mx.models.PaymentDate
import com.rayo.rayoxml.mx.services.Loan.LoanRenewViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalProposalLoadRequest
import com.rayo.rayoxml.mx.services.Loan.LoanRenewalViewModelFactory
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.mx.services.Loan.PLPLoanStep3Request
import com.rayo.rayoxml.mx.services.User.Prestamo
import com.rayo.rayoxml.mx.services.User.Solicitud
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.ui.dialogs.InfoDialogFragment
import com.rayo.rayoxml.mx.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.utils.CreditInformation
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.CreditParameters
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.mx.viewModels.RenewalViewModel
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


class RenewalProposalFragment(/*private val userParamsViewModel: UserViewModel*/) : Fragment() {

    private var _binding: FragmentRenewalProposalBinding? = null
    private val binding get() = _binding!!

    // Modelo compartido
    private val renewalViewModel: RenewalViewModel by activityViewModels()

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

    private val SUCCESSFUL_FORM_RESULT: String = "Formulario OK"

    //private var userData: Solicitud? = null
    private var personalData: LoanStepOneRequest? = null

    //
    private var proposalValue: Double = 0.0
    private var userData: Solicitud? = null
    private var hasMora: Boolean = false
    private lateinit var userPreferencesManager: PreferencesManager
    private lateinit var loanPreferencesManager: PreferencesManager
    private var loanType: String = ""
    private var adminDiscountString: String = ""
    private var tenDiscountString: String = ""
    private var discountRateString: String = ""
    private var userScore: String = "" // A+, A
    private var scoreExperianString: String = "0" // 500, 700

    private var plpSept3Data: PLPLoanStep3Request? = null

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
        if (formId != "" && userData != null && plpSept3Data != null) {
            if (loanType.isNotEmpty() && userData != null) {
                lifecycleScope.launch {
                    when(loanType){
                        "MINI" -> prestamoMini()
                        "RP" -> prestamoRP()
                        "PLP" -> prestamoPLP()
                    }
                }
            }
        }
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
                // Validar si se cargaron lso datos
                checkAndRun()
            }
        }

        viewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("RenewalProposalFragment", "Datos de usuario renovación: ${data}")
                userData = data
                if(userData?.descuentoAdministrativo!!.isNotEmpty()){
                    // Asignar estado descuentos
                    userData!!.descuentoAdministrativo?.let {
                        adminDiscountString = it
                        renewalViewModel.setAdminDiscount(it)
                    }
                    userData!!.descuento10?.let {
                        tenDiscountString = it
                        renewalViewModel.setTenDiscount(it)
                    }
                    // Obtener calificación
                    userScore = data.calificacion!!

                    Log.d("RenewalProposalFragment", "Datos de calificación de usuario: ${userScore}")
                }

                if(data.prestamos?.isNotEmpty() == true){
                    lastLoan = data.prestamos.first()
                }

                /*if (loanType.isNotEmpty() && userData != null) {
                    lifecycleScope.launch {
                        when(loanType){
                            "MINI" -> prestamoMini()
                            "RP" -> prestamoRP()
                            "PLP" -> prestamoPLP()
                        }
                    }
                }*/

                // Validar si se cargaron lso datos
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
            }
            view.post {
                setCreditData(loanValue, loanTerm)
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
                if (loanType.isNotEmpty() && userData != null) setCreditData(loanValue, loanTerm)
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
            // Avanza al siguiente paso
            (requireParentFragment() as? RenewalFragment)?.goToNextStep()
        }

        recyclerView = binding.creditDetailLoanProposal.recyclerViewPaymentsProposal
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoanPaymentAdapter(paymentDates)
        recyclerView.adapter = adapter
    }

    private fun checkAndDismissDialog() {
        loadingDialog.dismiss()
    }

    fun String.toBooleanSI(): Boolean {
        return this.equals("SI", ignoreCase = true)
    }

    private suspend fun prestamoMini(){
        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            val request = LoanRenewalProposalLoadRequest(
                userData?.contacto!!
            )
            // Guardar request
            renewalViewModel.setProposalLoadRequestData(request)

            val loanRenewalResponse = withContext(Dispatchers.IO) {
                loanRepository.getDataRenewalProposalLoad(request)
                ////loanViewModel.getData(request)
            }
            if(loanRenewalResponse != null){
                // Guardar response
                renewalViewModel.setProposalLoadResponseData(loanRenewalResponse)

                //loanViewModel.setRenewalData(loanRenewalResponse)
                // asignar datos
                proposalValue = loanRenewalResponse.solicitud.propuestaSugerida.toDouble()
                hasMora = loanRenewalResponse.solicitud.hasMora.toBooleanSI()

                // Asignar porcentaje de descuento (4+ préstamo)
                //Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRenewalResponse.solicitud.porcentajeDescuento}")
                discountRateString = loanRenewalResponse.solicitud.porcentajeDescuento
                renewalViewModel.setDiscountRate(loanRenewalResponse.solicitud.porcentajeDescuento)

                // Guardar score experian
                scoreExperianString = loanRenewalResponse.solicitud.score

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

    private suspend fun prestamoRP(){
        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            val request = LoanRenewalProposalLoadRequest(
                userData?.contacto!!
            )
            // Guardar request
            renewalViewModel.setProposalLoadRequestData(request)

            val loanRenewalResponse = withContext(Dispatchers.IO) {
                loanRepository.getDataRenewalStepFourPlusLoad(request)
            }
            if(loanRenewalResponse != null){
                // Guardar response
                renewalViewModel.setProposalLoadResponseData(loanRenewalResponse)

                //loanViewModel.setRenewalData(loanRenewalResponse)
                // asignar datos
                proposalValue = loanRenewalResponse.solicitud.propuestaSugerida.toDouble()
                hasMora = loanRenewalResponse.solicitud.hasMora.toBooleanSI()

                // Asignar porcentaje de descuento (4+ préstamo)
                Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRenewalResponse.solicitud.porcentajeDescuento}")
                discountRateString = loanRenewalResponse.solicitud.porcentajeDescuento
                renewalViewModel.setDiscountRate(loanRenewalResponse.solicitud.porcentajeDescuento)

                // Guardar score experian
                scoreExperianString = loanRenewalResponse.solicitud.score

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

    private suspend fun prestamoPLP(){
        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {
            // Cargar datos de propuesta
            var request = plpSept3Data!!
            // Asignar formulario
            request.formulario = formId

            Log.d("RenewalPlpStep3Fragment", "STEP 3 form: ${request}")

            // Guardar request
            //renewalViewModel.setProposalLoadRequestData(request)

            val loanRenewalResponse = withContext(Dispatchers.IO) {
                loanRepository.getDataPlpStep3(request)
            }

            Log.d("RenewalProposalFragment", "PLP loan step 3 Data: ${loanRenewalResponse}")

            if(loanRenewalResponse != null && loanRenewalResponse.solicitud.codigo == "200"){
                // Guardar response
                //renewalViewModel.setProposalLoadResponseData(loanRenewalResponse)

                //loanViewModel.setRenewalData(loanRenewalResponse)
                // asignar datos
                proposalValue = loanRenewalResponse.solicitud.propuesta.toDouble()
                hasMora = loanRenewalResponse.solicitud.hasMora.toBooleanSI()

                // Asignar porcentaje de descuento (4+ préstamo)
                //Log.d("RenewalProposalFragment", "Obteniendo porcentaje de descuento: ${loanRenewalResponse.solicitud.porcentajeDescuento}")
                //discountRateString = loanRenewalResponse.solicitud.porcentajeDescuento
                //renewalViewModel.setDiscountRate(loanRenewalResponse.solicitud.porcentajeDescuento)
                discountRateString = "0"
                renewalViewModel.setDiscountRate("0")

                // Guardar score experian
                scoreExperianString = loanRenewalResponse.solicitud.score

                loadDataPLP()

                // Forzar la ejecución del listener al inicio
                binding.checkboxUserTerms.setOnCheckedChangeListener(null) // Remove previous listener
                binding.checkboxUserTerms.jumpDrawablesToCurrentState() // Avoid weird animations
                binding.checkboxUserTerms.callOnClick() // Force execution of the listener

                checkAndDismissDialog() // Ocultar dialog
            }else{
                loadingDialog.dismiss() // ocultar loading
            }
            Log.d("RenewalProposalFragment", "Datos api: ${loanRenewalResponse}")
        } catch (e: Exception) {
            checkAndDismissDialog() // Ocultar en caso de error
            Log.e("RenewalProposalFragment", "Error obteniendo datos", e)
            // Mostrar errores
        }
    }

    private fun loadData(){

        // Cargar variables de crédito
        selectedCountry = CreditParameterManager.getSelectedCountry()
        creditParams = CreditParameterManager.getCreditParameters()!!
        creditInfo = CreditInformationManager.getCreditInformation()!!

        if (creditParams != null && creditInfo != null) {
            Log.d("RenewalProposalFragment", "Country: $selectedCountry - Data: $creditParams")
            Log.d("RenewalProposalFragment", "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo")
        } else {
            Log.e("RenewalProposalFragment", "No credit parameters found!")
        }

        val loanValueTextView: TextView = binding.root.findViewById<TextView>(R.id.value_amount_proposal)
        val proposalValue = proposalValue
        Log.d("RenewalProposalFragment", "Propuesta: $proposalValue")
        val formattedValue = formatter.format(proposalValue?.toDouble())
        loanValueTextView.text = "$$formattedValue $countryCurrency"

        //
        // Variables globales para cálculo de crédito
        //var loanValue = proposalValue!!.toInt();
        //var loanTerm = 1;
        loanValue = proposalValue.toInt();
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

        // Datos CreditInformation
        val textViewImportant: TextView = binding.root.findViewById<TextView>(R.id.text_final_credit_detail)
        if (creditInfo != null) {
            textViewImportant.text = "Importante: ${creditInfo!!.important}"
        }
    }

    private fun loadDataPLP(){

        // Cargar variables de crédito
        selectedCountry = CreditParameterManager.getSelectedCountry()
        creditParams = CreditParameterManager.getCreditParameters()!!
        creditInfo = CreditInformationManager.getCreditInformation()!!

        if (creditParams != null && creditInfo != null) {
            Log.d("RenewalProposalFragment", "Country: $selectedCountry - Data: $creditParams")
            Log.d("RenewalProposalFragment", "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo")
        } else {
            Log.e("RenewalProposalFragment", "No credit parameters found!")
        }

        val loanValueTextView: TextView = binding.root.findViewById<TextView>(R.id.value_amount_proposal)
        val proposalValue = proposalValue
        Log.d("RenewalProposalFragment", "Propuesta: $proposalValue")
        val formattedValue = formatter.format(proposalValue?.toDouble())
        loanValueTextView.text = "$$formattedValue $countryCurrency"

        //
        // Variables globales para cálculo de crédito
        //var loanValue = proposalValue!!.toInt();
        //var loanTerm = 1;
        loanValue = proposalValue.toInt();
        loanTerm = 12;

        val deadLineLayout: LinearLayout = binding.root.findViewById<LinearLayout>(R.id.deadlineLayout)
        deadLineLayout.visibility = View.GONE
        val deadLineContainer: LinearLayout = binding.root.findViewById<LinearLayout>(R.id.containerTextDeadline)
        deadLineContainer.visibility = View.GONE
        val deadLineCardview: CardView = binding.root.findViewById<CardView>(R.id.deadlineCardview)
        deadLineCardview.visibility = View.GONE

        val deadLineSeekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.deadlineSeekbar)
        deadLineSeekBar.visibility = View.GONE
        val deadLineText: TextView = binding.root.findViewById<TextView>(R.id.deadlineText)
        val seekbarDeadLineMinValue: TextView = binding.root.findViewById<TextView>(R.id.minValueDeadline)
        seekbarDeadLineMinValue.visibility = View.GONE
        val seekbarDeadLineMaxValue: TextView = binding.root.findViewById<TextView>(R.id.maxValueDeadline)
        seekbarDeadLineMaxValue.visibility = View.GONE
        // Listener deadline seekbar
        var deadLineMinValue = 1
        var deadLinMaxValue = 1

        // Asignar valor plazo mínimo por defecto previo a poner foco sobre controles
        deadLineText.text = "12 Cuotas"
        deadLineText.visibility = View.GONE

        /*deadLineMinValue = 1
        deadLinMaxValue = creditParams!!.maxQuotes
        deadLineSeekBar.max = deadLinMaxValue - 1
        deadLineSeekBar.progress = 0
        // Valores de cuotas min y max
        seekbarDeadLineMinValue.text = "$deadLineMinValue cuota"
        seekbarDeadLineMaxValue.text = "$deadLinMaxValue cuotas"*/

        // Datos CreditInformation
        val textViewImportant: TextView = binding.root.findViewById<TextView>(R.id.text_final_credit_detail)
        if (creditInfo != null) {
            textViewImportant.text = "Importante: ${creditInfo!!.important}"
        }
    }

    private fun setCreditData(creditValue: Int, term: Int){
        realTerm = term * 15
        val rate = creditParams?.interestRate //0.00064879
        val ivaRate = creditParams?.tax //19
        var loanInterest = ceil((creditValue * rate!!) * realTerm)

        // Obtener costo de tech
        val techValue = getTecnValue(loanType, creditValue, realTerm, technologyCheck, userScore)

        var techIva = ceil((techValue * ivaRate!!) / 100)
        //Log.d("RenewalProposalFragment", "adminValue: $adminValue - techValue: $techValue")

        // Validar si aplica porcentaje descuento
        var loanDiscount = 0.0
        if( tenDiscountString.toBooleanSI() ){
            loanDiscount = creditValue.toDouble() * (10.0 / 100.0)
        }else if( discountRateString.isNotEmpty() ){
            val discountRate = discountRateString.toDouble()
            loanDiscount = creditValue.toDouble() * (discountRate / 100.0)
        }
        Log.d("RenewalProposalFragment", "Descuento aplicado: ${loanDiscount}")
        Log.d("RenewalProposalFragment", "Descuento 10: $tenDiscountString")
        Log.d("RenewalProposalFragment", "Descuento porcentaje: $discountRateString")

        val scoreExperian = scoreExperianString.toDouble()
        var fianza = 0.0
        var fianzaAsignada = 0.06

        fianzaAsignada = when {
            scoreExperian in 450.0..549.99 -> if (!hasMora) 0.05 else 0.06
            scoreExperian in 550.0..699.99 -> if (!hasMora) 0.0422 else 0.05
            scoreExperian >= 700.0 -> if (!hasMora) 0.0 else 0.0422
            else -> fianzaAsignada
        }

        fianza = ceil((creditValue * fianzaAsignada) * 1.19)

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
        val total = subtotal + techValue + techIva + fianza - loanDiscount
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

        // Descuentos
        val discountField: TextView = binding.root.findViewById<TextView>(R.id.discount_value_proposal)
        // Mostrar campos
        val containerLayout = binding.root.findViewById<LinearLayout>(R.id.discountContainer)
        containerLayout.visibility = View.VISIBLE

        val discountFormattedValue = formatter.format(loanDiscount)
        discountField.text = "$$discountFormattedValue"

        // Calcular fechas de pagos
        val newPaymentDates = calculatePaymentDates(term, total, Date())
        /*for (payment in newPaymentDates) {
            println("Fecha: ${payment.date}, Monto: $${formatter.format(payment.amount)}")
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


    fun getTecnValue(
        tipoPrestamo: String,
        valorSolicitar: Int,
        plazoSeleccionado: Int,
        pagaTecno: Boolean,
        calificacion: String
    ): Double {
        if (!pagaTecno) return 0.0

        var pTasa = 1100
        if (calificacion == "A+" || calificacion == "A") {
            pTasa = 1000
        }

        return when (tipoPrestamo) {
            "MINI" -> ceil(plazoSeleccionado * pTasa + (valorSolicitar * 0.125))
            "RP" -> ceil(plazoSeleccionado * pTasa + (valorSolicitar * 0.2))
            "PLP" -> ceil((valorSolicitar * 0.33 + (valorSolicitar * 0.25)))
            else -> 0.0
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

    private fun openInfoDialog(title: String, message: String) {
        val dialog = InfoDialogFragment.newInstance(title, message)
        dialog.show(parentFragmentManager, "InfoDialogFragment")
    }
}