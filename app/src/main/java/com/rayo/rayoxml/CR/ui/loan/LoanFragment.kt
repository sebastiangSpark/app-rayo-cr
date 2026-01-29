package com.rayo.rayoxml.cr.ui.loan

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.adapters.LoanPaymentAdapter
import com.rayo.rayoxml.cr.models.PaymentDate
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.databinding.FragmentLoanBinding
import com.rayo.rayoxml.cr.ui.dialogs.InfoDialogFragment
import com.rayo.rayoxml.utils.CreditInformation
import com.rayo.rayoxml.utils.CreditInformationManager
import com.rayo.rayoxml.utils.CreditParameterManager
import com.rayo.rayoxml.utils.CreditParameters
import com.rayo.rayoxml.utils.PreferencesManager
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.round

class LoanFragment : Fragment() {

    private var _binding: FragmentLoanBinding? = null
    private val binding get() = _binding!!

    private val currencySymbol = "₡"
    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

    private var selectedCountry: String = ""
    private var creditParams: CreditParameters? = null
    private var creditInfo: CreditInformation? = null

    // Recyclerview de pagos
    private var paymentDates = mutableListOf<PaymentDate>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanPaymentAdapter
    val text = "Importante: Si incurres en mora, se aplicará la tasa del 27,90% EA sobre el valor en mora. el valor de intereses de mora será el máximo permitido más los gastos de cobranza que apliquen según T&C."

    // Asignar viewModel para obtener datos de simulador en caso de requerirse
    private lateinit var userPreferencesManager: PreferencesManager

    private val viewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(
            UserRepository(),
            preferencesManager = userPreferencesManager
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userPreferencesManager = PreferencesManager(requireContext()) // Inicialización
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanBinding.inflate(layoutInflater, container, false) //_binding = FragmentLoanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
//                findNavController().navigate(R.id.selectCountryFragment)
            }
        })

        formatter.applyPattern("#,##0.00")  // Ensures two decimal places

        // Recyclerview de pagos
        recyclerView = binding.creditDetail.recyclerViewPayments
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoanPaymentAdapter(paymentDates)
        recyclerView.adapter = adapter

        // Alerts informativos
        val btnInfoInterest: ImageView = binding.root.findViewById(R.id.btn_info_interest)
        val btnInfoTecnology: ImageView = binding.root.findViewById(R.id.btn_info_tecnology)
        val btnInfoIVA: ImageView = binding.root.findViewById(R.id.btn_info_IVA)

        btnInfoInterest.setOnClickListener {
            openInfoDialog(getString(R.string.interest_info_title), getString(R.string.interest_info_content))
        }
        btnInfoTecnology.setOnClickListener {
            openInfoDialog(getString(R.string.technology_info_title), getString(R.string.technology_info_content))
        }
        btnInfoIVA.setOnClickListener {
            openInfoDialog(getString(R.string.iva_info_title), getString(R.string.iva_info_content))
        }

        val btnNavigate = binding.root.findViewById<Button>(R.id.credit_navigate)

       btnNavigate.setOnClickListener {
           try {
                findNavController().navigate(R.id.action_loanFragment_to_formFragment)
           } catch (e: Exception) {
               e.printStackTrace()
           }
       }

        val spannable = SpannableString(text)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.textFinalCreditDetail?.text = spannable

        // Cargar datos y gestionar eventos de controles
        view.post{
            this.loadData();
        }
    }

    private fun loadData(){

        try {
            // Cargar variables de crédito
            selectedCountry = CreditParameterManager.getSelectedCountry()
            creditParams = CreditParameterManager.getCreditParameters()!!
            creditInfo = CreditInformationManager.getCreditInformation()!!

            // Variables globales para cálculo de crédito
            var loanValue = 0;
            var loanTerm = 1;

            if (creditParams != null && creditInfo != null) {
                Log.d("LoanFragment", "Country: $selectedCountry - Data: $creditParams")
                Log.d("LoanFragment", "Country: ${CreditInformationManager.getSelectedCountry()} - Data: $creditInfo")
            } else {
                Log.e("LoanFragment", "No credit parameters found!")
            }

            val seekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.sliderOne)
            val textView: TextView = binding.root.findViewById<TextView>(R.id.numberText)
            val seekbarMinValue: TextView = binding.root.findViewById<TextView>(R.id.minValue)
            val seekbarMaxValue: TextView = binding.root.findViewById<TextView>(R.id.maxValue)

            val deadLineSeekBar: SeekBar = binding.root.findViewById<SeekBar>(R.id.deadlineSeekbar)
            val deadLineText: TextView = binding.root.findViewById<TextView>(R.id.deadlineText)
            val seekbarDeadLineMinValue: TextView = binding.root.findViewById<TextView>(R.id.minValueDeadline)
            val seekbarDeadLineMaxValue: TextView = binding.root.findViewById<TextView>(R.id.maxValueDeadline)

            // Listener seekbar
            var minValue = 0
            var maxValue = 0
            var step = 1       // Step increment

            minValue = creditParams!!.minAmount
            maxValue = creditParams!!.maxAmount
            step = creditParams!!.ranges
            //seekBar.max = maxValue - minValue  // Adjust max to compensate for offset
            seekBar.max = (maxValue - minValue) / step  // Adjust max to match step size
            seekBar.progress = 0  // Default position (corresponds to minValue)
            // Valores de préstamo min y max
            seekbarMinValue.text = currencySymbol + formatter.format(minValue)
            seekbarMaxValue.text = currencySymbol + formatter.format(maxValue)

            // Asignar valores por defecto previo a poner foco sobre controles
            //val formattedValue = NumberFormat.getNumberInstance(Locale.US).format(seekBar.progress + minValue)
            val formattedValue = formatter.format(seekBar.progress + minValue)
            textView.text = "$currencySymbol$formattedValue"

            //seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    //val actualValue = progress + minValue // Shift progress to start at minValue
                    val actualValue = minValue + (progress * step) // Scale value
                    //val formattedValue = NumberFormat.getNumberInstance(Locale.US).format(actualValue)
                    val formattedValue = formatter.format(actualValue)
                    textView.text = "$currencySymbol$formattedValue"  // Example: $10,000

                    // Cálculo de valores de crédito
                    loanValue = actualValue
                    setCreditData(loanValue, loanTerm)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
            // Fin listener seekbar

            // Asociar listener al SeekBar
            seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

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

            deadLineSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
            })
            // Fin listener deadline seekbar

            // simular evento para inicializar fechas
            // seekBarChangeListener debe ejecutarse DESPUÉS de la inicialización
            view?.post {
                seekBarChangeListener.onProgressChanged(seekBar, 0, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCreditData(creditValue: Int, term: Int){
        val realTerm = term * 15
        val loanInterest = setInterestValue(creditValue, term)
        val techValue = setPTechValue(creditValue, term);
        val techIva = setIvaValue(techValue)

        // Update UI with credit data

        // interés
        val loanInterestField: TextView = binding.root.findViewById<TextView>(R.id.interest_value)
        val loanInterestformattedValue = formatter.format(loanInterest)
        loanInterestField.text = "$currencySymbol$loanInterestformattedValue"

        // tech
        val techField: TextView = binding.root.findViewById<TextView>(R.id.technology_value)
        val techFormattedValue = formatter.format(techValue)
        techField.text = "$currencySymbol$techFormattedValue"

        // iva tech
        val ivaTechField: TextView = binding.root.findViewById<TextView>(R.id.IVAtechnology_value)
        val ivaTechFormattedValue = formatter.format(techIva)
        ivaTechField.text = "$currencySymbol$ivaTechFormattedValue"

        // Subtotal
        val subtotal = creditValue
        val subtotalField: TextView = binding.root.findViewById<TextView>(R.id.subtotal_value)
        val subtotalFormattedValue = formatter.format(subtotal)
        subtotalField.text = "$currencySymbol$subtotalFormattedValue"

        // Total a pagar
        val total = subtotal + techValue + techIva + loanInterest
        val totalField: TextView = binding.root.findViewById<TextView>(R.id.total_to_pay_value)
        val totalFormattedValue = formatter.format(total)
        totalField.text = "$currencySymbol$totalFormattedValue"

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
        viewModel.updateLoanTerm(realTerm)
        viewModel.updateLoanAmount(creditValue)
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

    private fun calculatePaymentDates(loanTerm: Int, feeAmount: Double, baseDate: Date): MutableList<PaymentDate> {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}