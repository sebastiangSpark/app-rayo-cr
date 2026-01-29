package com.rayo.rayoxml.mx.ui.home

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.OptionsInstallmentBottomAdapter
import com.rayo.rayoxml.mx.adapters.PrincipalCardLoanAdapter
import com.rayo.rayoxml.databinding.FragmentHomeBinding
import com.rayo.rayoxml.databinding.FragmentPrincipalCardLoanBinding
import com.rayo.rayoxml.mx.models.Card
import com.rayo.rayoxml.mx.models.PrincipalLoanCardData
import com.rayo.rayoxml.mx.services.User.Prestamo
import com.rayo.rayoxml.mx.services.User.Solicitud
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.ui.dialogs.BottomSheetDialog
import com.rayo.rayoxml.mx.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.mx.ui.slider_cards.CardAdapter
import com.rayo.rayoxml.utils.ContactoPyZ
import com.rayo.rayoxml.utils.PDFGenerator
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.utils.PrestamoPyZ
import com.rayo.rayoxml.utils.navigateToRenewalFragment
import com.rayo.rayoxml.mx.viewModels.RenewalViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardBinding: FragmentPrincipalCardLoanBinding
    private lateinit var principalCardLoanAdapter: PrincipalCardLoanAdapter

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel
    private var userHasLoans: Boolean = false
    private var userHasActiveLoans: Boolean = true
    private var userContactId: String = ""
    private var userData: Solicitud? = null

    // Modelo compartido renovaciones
    private val renewalViewModel: RenewalViewModel by activityViewModels()

    // Activar botones préstamos RP y PLP
    private var buttonRP: Boolean = false
    private var buttonPLP: Boolean = false

    // Tipos de préstamo
    private val PRESTAMO_MINI = "Préstamo mini"
    private val PRESTAMO_RAYO_PLUS = "Préstamo Rayo Plus"
    private val PRESTAMO_LARGO_PLAZO = "Préstamo Largo Plazo (PLP)"
    private val PRESTAMO_EXTRANJERO = "Préstamo Extranjeros"
    // Lista completa de préstamos
    val listaPrestamos: MutableList<Prestamo> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        cardBinding = FragmentPrincipalCardLoanBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun String.toBooleanSI(): Boolean {
        return this.equals("SI", ignoreCase = true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupDotsIndicator()

        val scrollView = view.findViewById<ScrollView>(R.id.mainActivityScrollView)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.overScrollMode = View.OVER_SCROLL_NEVER

        // Recuperar el ViewModel compartido
        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##0.00")

        // Loading
        val loadingDialog = LoadingDialogFragment()

        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                userData = user
                if (user.prestamos?.isNotEmpty() == true) {
                    userHasLoans = true
                    userContactId = user.contacto!!
                    userHasActiveLoans = hasActiveLoans(user.prestamos)
                    Log.d("HomeFragment", "Usuario con préstamos activos: $userHasActiveLoans")

                    // Activar botones de préstamos
                    if(!userHasActiveLoans) binding.containerQuickAction.findViewById<ImageView>(R.id.MiniLock).visibility = View.GONE
                    buttonRP = user.BotonRP.toString().toBooleanSI()
                    if(buttonRP) binding.containerQuickAction.findViewById<ImageView>(R.id.RpLock).visibility = View.GONE
                    buttonPLP = user.BotonPLP.toString().toBooleanSI()
                    if(buttonPLP) binding.containerQuickAction.findViewById<ImageView>(R.id.PlpLock).visibility = View.GONE

                    // Procesar tipos de préstamos
                    if (user.prestamos != null) {
                        // Asignar tipo
                        user.prestamos.forEach { it.tipo = PRESTAMO_MINI }
                    }
                    if (user.prestamosRP != null) {
                        // Asignar tipo
                        user.prestamosRP.forEach { it.tipo = PRESTAMO_RAYO_PLUS }
                    }
                    if (user.prestamosPLP != null) {
                        // Asignar tipo
                        user.prestamosPLP.forEach { it.tipo = PRESTAMO_LARGO_PLAZO }
                    }
                    if (user.prestamosExtranjeros != null) {
                        // Asignar tipo
                        user.prestamosExtranjeros.forEach { it.tipo = PRESTAMO_EXTRANJERO }
                    }

                    listaPrestamos.clear()
                    listaPrestamos.addAll(user.prestamos!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosRP!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosPLP!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosExtranjeros!!.filterNotNull() ?: emptyList())

                    val firstLoan = listaPrestamos.get(0)
                    if (firstLoan != null) {
                        val loans = listaPrestamos ?: emptyList()
                        val cards = loans.map { loan ->
                            if (loan.pagos.isNotEmpty()) {
                                val firstLoanPayment = loan.pagos.get(0)
                                PrincipalLoanCardData(
                                    amount = "$${formatter.format(loan.montoPrestado.toDoubleOrNull())}",
                                    state = loan.estado,
                                    payment = "Cuota: $${formatter.format(firstLoanPayment.montoPagoActual.toDouble())}",
                                    date = "Próximo pago: ${firstLoanPayment.fechaPago}",
                                    paymentId = firstLoanPayment.pagoId,
                                    loanId = loan.prestamoId,
                                    loanType = loan.tipo,
                                    disbursementDate = loan.fechaDesembolso,
                                    code = loan.codigo
                                )
                            }else if(loan.fechaDesembolso == "Pendiente Desembolso"){
                                Log.d("HomeFragment", "Primer préstamo sin desembolsar")
                                PrincipalLoanCardData(
                                    amount = "$${formatter.format(loan.montoPrestado.toDoubleOrNull())}",
                                    state = loan.fechaDesembolso,
                                    payment = "Cuota: ${loan.fechaDesembolso}",
                                    date = "Próximo pago: ${loan.fechaDesembolso}",
                                    paymentId = loan.prestamoId,
                                    loanId = loan.prestamoId,
                                    loanType = loan.tipo,
                                    disbursementDate = loan.fechaDesembolso,
                                    code = loan.codigo
                                )
                            }else{
                                Log.d("HomeFragment", "Primer préstamo sin pagos")
                                PrincipalLoanCardData(
                                    amount = "$${formatter.format(loan.montoPrestado.toDoubleOrNull())}",
                                    state = loan.estado,
                                    payment = "Cuota:",
                                    date = "Próximo pago: ",
                                    paymentId = loan.prestamoId,
                                    loanId = loan.prestamoId,
                                    loanType = loan.tipo,
                                    disbursementDate = loan.fechaDesembolso,
                                    code = loan.codigo
                                )
                            }
                        }
                        principalCardLoanAdapter.setItems(cards)
                    } else {
                        Log.d("HomeFragment", "Primer préstamo no encontrado")
                    }

                } else {
                    Log.d("HomeFragment", "Usuario sin préstamos")
                }
            }
            loadingDialog.dismiss() // Ocultar loading después de recibir respuesta

            // Botones de créditos activo/inactivo
            val hasActiveLoan = checkIfUserHasActiveLoan()

            if (hasActiveLoan) {
                cardBinding.cardLoanActive.visibility = View.VISIBLE
                binding.cardLoanInactive.visibility = View.GONE
                // TEST
                /*if(user?.contacto == "003O400000MkyxNIAR"){
                    binding.cardLoanInactive.visibility = View.VISIBLE
                }*/
                binding.containerQuickAction.visibility = View.VISIBLE

            } else {
                cardBinding.cardLoanActive.visibility = View.GONE
                binding.cardLoanInactive.visibility = View.VISIBLE
                binding.containerQuickAction.visibility = View.GONE
            }

            if (hasActiveLoan) {
                cardBinding.cardLoanActive.visibility = View.VISIBLE
                binding.cardLoanInactive.visibility = View.GONE
                // TEST
                /*if (user?.contacto == "003O400000MkyxNIAR") {
                    binding.cardLoanInactive.visibility = View.VISIBLE
                }*/
                binding.containerQuickAction.visibility = View.VISIBLE

            } else {
                cardBinding.cardLoanActive.visibility = View.GONE
                binding.cardLoanInactive.visibility = View.VISIBLE
                binding.containerQuickAction.visibility = View.GONE
            }
        }
        // Fin Observar los datos del usuario

        val recyclerView = binding.recyclerViewCards
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        val cards = listOf(
            Card(
                R.drawable.ic_bars_yellow,
                "Aumento de cupo en tus siguientes renovaciones"),
            Card(
                R.drawable.ic_money,
                "Línea de crédito hasta por \$15.000 MXN desde tu préstamo N° 13"
            ),
            Card(
                R.drawable.ic_cupon,
                "Cupones de descuento desde el 8% al 20% en tus siguientes préstamos"
            ),
            Card(
                R.drawable.ic_boletas,
                "Sorteos de boletas de cine. Bonos de consumo Electrodomésticos"
            )
        )

        val adapter = CardAdapter(cards)
        recyclerView.adapter = adapter

        binding.buttonSeeText.setOnClickListener {
            goToPersonalLoan()
        }

        // Solicitar crédito
        binding.firstButtonLoanHome.setOnClickListener {
            findNavController().navigate(R.id.loanFragment)
        }
        setupButtonListeners()
        setupQuickActionCards(view)
    }
    /*
        private fun setupViewPager() {
            principalCardLoanAdapter = PrincipalCardLoanAdapter()
            binding.viewPagerPrincipalCards.adapter = principalCardLoanAdapter
            binding.viewPagerPrincipalCards.offscreenPageLimit = 2
        }*/

    private fun setupViewPager() {
        principalCardLoanAdapter = PrincipalCardLoanAdapter(
            onButtonOpenBottomSheetClick = { item ->
                showBottomSheetDialogOptions(item)
            },
            onButtonOpenPaymentInstallmentClick = { item ->
                showPayInstallmentDetailsDialog(item)
            }
        )
        binding.viewPagerPrincipalCards.adapter = principalCardLoanAdapter
        binding.viewPagerPrincipalCards.offscreenPageLimit = 2
    }

    private fun setupDotsIndicator() {
        // Conectar el DotsIndicator al ViewPager2
        binding.principalDotsIndicator.setViewPager2(binding.viewPagerPrincipalCards)
    }

    private fun showBottomSheetDialogOptions(item: PrincipalLoanCardData) {
        val dialogView =
            layoutInflater.inflate(R.layout.bottom_sheet_dialog_options_installment, null)
        val dialog =
            com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
                setContentView(dialogView)
            }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewPayInstallment)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Lista de opciones (solo texto)
        val items = listOf(
            OptionsInstallmentBottomAdapter.OptionWithIcon("Pagar cuota", R.drawable.ic_card),
            OptionsInstallmentBottomAdapter.OptionWithIcon("Ver Pagos", R.drawable.ic_eye),
            OptionsInstallmentBottomAdapter.OptionWithIcon("Paz y Salvo", R.drawable.ic_count_fees)
        )

        // Configurar el adapter
        recyclerView.adapter = OptionsInstallmentBottomAdapter(items) { selectedOption ->
            // Manejar la opción seleccionada
            when (selectedOption) {
                "Pagar cuota" -> {
                    // Acción para "Pagar cuota"
                    showPayInstallmentDetailsDialog(item)
                }

                "Ver Pagos" -> {
                    // Acción para "Ver Pagos"
                    Toast.makeText(
                        requireContext(),
                        "Ver Pagos seleccionado para ${item.amount}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                "Paz y Salvo" -> {
                    // Acción para "Paz y Salvo"
                    /*Toast.makeText(
                        requireContext(),
                        "Paz y Salvo seleccionado para ${item.amount}",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    val contacto = ContactoPyZ(userData?.nombre!!, userData?.apellidos!!, userData?.documento!!)
                    val prestamo = PrestamoPyZ(item.loanId, item.code, item.disbursementDate)
                    PDFGenerator.createAndDownloadPdf(requireContext(), contacto, prestamo)
                }
            }
            dialog.dismiss() // Cerrar el diálogo después de seleccionar una opción
        }

        dialog.show()
    }

    private fun showPayInstallmentDetailsDialog(item: PrincipalLoanCardData) {
        val dialogView =
            layoutInflater.inflate(R.layout.bottom_sheet_dialog_payment_installment, null)
        val dialog =
            com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
                setContentView(dialogView)
            }
        // Configurar los datos en el diálogo
        /*        val textType = dialogView.findViewById<TextView>(R.id.text_value_type)
                val textInstallment = dialogView.findViewById<TextView>(R.id.text_value_installment)
                val textInstallmentPending = dialogView.findViewById<TextView>(R.id.text_value_installment_pending)
                val textPay = dialogView.findViewById<TextView>(R.id.text_value_pay)
                val textDateInstallment = dialogView.findViewById<TextView>(R.id.text_value_date_installment)
                val textAmount = dialogView.findViewById<TextView>(R.id.text_value_amount)

                textType.text = "Monto: ${item.amount}"
                textInstallment.text = "Fecha de vencimiento: ${item.date}"
                textInstallmentPending.text = "Estado: ${item.state}"
                textPay.text = "Estado: ${item.state}"
                textDateInstallment.text = "Estado: ${item.state}"
                textAmount.text = "Estado: ${item.state}"*/

        val selectedItem = item
        if (selectedItem != null) {
            Log.d("HomeFragment", "Seleccionado: " + selectedItem)
        } else {
            Log.d("HomeFragment", "Ninguno Seleccionado")
        }

        val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        val buttonAction = dialogView.findViewById<Button>(R.id.buttonAction)
        buttonAction.setOnClickListener {
            showPaymentMethodDialog(selectedItem.paymentId)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPaymentMethodDialog(paymentId: String) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_dialog_payment_methods_installment, null)

        // Crear el diálogo
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        // Obtener referencias a las vistas
        val radioGroupPaymentMethods = dialogView.findViewById<RadioGroup>(R.id.radioGroupPaymentMethods)
        val buttonPay = dialogView.findViewById<Button>(R.id.buttonPay)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val linearLayoutCreditCard = dialogView.findViewById<LinearLayout>(R.id.linearLayoutCreditCard)
        val linearLayoutBankTransfer = dialogView.findViewById<LinearLayout>(R.id.linearLayoutBankTransfer)

        // Configurar listeners para los LinearLayout
        linearLayoutCreditCard.setOnClickListener {
            radioGroupPaymentMethods.check(R.id.radioCreditCard)
        }

        linearLayoutBankTransfer.setOnClickListener {
            radioGroupPaymentMethods.check(R.id.radioBankTransfer)
        }

        // Configurar listener para el botón de Pagar
        buttonPay.setOnClickListener {
            val selectedId = radioGroupPaymentMethods.checkedRadioButtonId

            if (selectedId != -1) { // Si hay una opción seleccionada
                val selectedPaymentMethod = when (selectedId) {
                    R.id.radioCreditCard -> "Pagos Seguros en Linea"
                    R.id.radioBankTransfer -> "Botón Bancolombia"
                    else -> "" // Este caso no debería ocurrir porque ya verificamos que selectedId != -1
                }
                //Toast.makeText(requireContext(), "Pagando con: $selectedPaymentMethod", Toast.LENGTH_SHORT).show()
                if(selectedPaymentMethod == "Pagos Seguros en Linea"){
                    val url = "https://rayocol--develop.sandbox.my.site.com/RecaudosRayoColombia/?idContacto=$userContactId&idPago=$paymentId"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }else {
                    val url = "https://rayocol--develop.sandbox.my.site.com/RecaudosRayoColombia/?idContacto=$userContactId&idPago=$paymentId"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Selecciona una forma de pago", Toast.LENGTH_SHORT).show()
            }
        }

        val radioCreditCard = dialogView.findViewById<RadioButton>(R.id.radioCreditCard)
        val radioBankTransfer = dialogView.findViewById<RadioButton>(R.id.radioBankTransfer)

        // Crear un ColorStateList programáticamente
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // Estado seleccionado
                intArrayOf(-android.R.attr.state_checked) // Estado no seleccionado
            ),
            intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.Matisse_700), // Color seleccionado
                ContextCompat.getColor(requireContext(), R.color.woodsmoke_600) // Color no seleccionado
            )
        )

// Aplicar el ColorStateList a los RadioButton
        radioCreditCard.buttonTintList = colorStateList
        radioBankTransfer.buttonTintList = colorStateList

        // Configurar listener para el botón de Cancelar
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Mostrar el diálogo
        dialog.show()
    }


    private fun setupQuickActionCards(view: View) {
        val cardClockArrow: CardView = view.findViewById(R.id.card_clock_arrow)
        val cardLightning: CardView = view.findViewById(R.id.card_lightning)
        val cardVector: CardView = view.findViewById(R.id.card_vector)

        cardClockArrow.setOnClickListener {
            // Validar si se puede renovar
            if(isEnabledLoanMiniRenovation()){
                // Asignar tipo de préstamo a renovar
                renewalViewModel.setLoanType("MINI")
                navigateToRenewalFragment()
            }else{
                openInfoDialog("No puedes renovar mini", "En este momento no puedes renovar un mini porque tienes un préstamo pendiente. Si te surgen dudas, contáctanos con gusto te ayudaremos", "Entendido")
            }
        }
        cardLightning.setOnClickListener {
            //buttonRP = true
            if(buttonRP){
                // Asignar tipo de préstamo a renovar
                renewalViewModel.setLoanType("RP")
                navigateToRenewalFragment()
            }else{
                openInfoDialog("No puedes renovar Rayo Plus", "En este momento no puedes renovar un Rayo Plus porque tienes un préstamo pendiente. Si te surgen dudas, contáctanos para tener el placer de ayudarte", "Entendido")
            }
        }
        cardVector.setOnClickListener {
            if(buttonPLP){
                // Asignar tipo de préstamo a renovar
                renewalViewModel.setLoanType("PLP")
                navigateToRenewalFragment()
            }else{
                openInfoDialog("Tu historial te acerca a Rayo Largo Plazo", "Rayo Largo Plazo es una opción exclusiva para clientes con un excelente historial de pagos. Continúa utilizando nuestros productos y pronto podrás acceder a financiamiento con mayor flexibilidad y plazos de pago extendidos.", "Entendido")
            }
        }
    }


    private fun openInfoDialog(title: String, message: String, buttonText: String) {
        val bottomSheetDialog = BottomSheetDialog.newInstance(title, message, buttonText)
        bottomSheetDialog.show(childFragmentManager, "BottomSheetDialog")
    }

    private fun checkIfUserHasActiveLoan(): Boolean {
        return userHasLoans
    }

    private fun setupButtonListeners() {
        binding.buttonSeeText.setOnClickListener {
            goToPersonalLoan()
        }
    }

    private fun goToPersonalLoan() {
        val bottomNavView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavViewLogin)
        bottomNavView.selectedItemId = R.id.personalLoanFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Validar si cumple requisitos para renovar préstamo mini
    private fun isEnabledLoanMiniRenovation(): Boolean{

        return if(userHasActiveLoans) false
        else true

        // Test
        /*if(userContactId == "003O400000nplhFIAQ"){
            Log.d("HomeFragment", "Saltando restricción de usuario para renovar")
            return true
        }
        else if(userHasActiveLoans) return false
        else return true*/
        //return true
    }

    private fun hasActiveLoans(loans: List<Prestamo>): Boolean {
        return loans.any { it.estado != "Cancelado" }
    }
}