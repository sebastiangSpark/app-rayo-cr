package com.rayo.rayoxml.cr.ui.home

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
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.adapters.OptionsInstallmentBottomAdapter
import com.rayo.rayoxml.cr.adapters.PrincipalCardLoanAdapter
import com.rayo.rayoxml.databinding.FragmentHomeBinding
import com.rayo.rayoxml.databinding.FragmentPrincipalCardLoanBinding
import com.rayo.rayoxml.cr.models.Card
import com.rayo.rayoxml.cr.models.PrincipalLoanCardData
//import com.rayo.rayoxml.cr.services.User.Prestamo
//import com.rayo.rayoxml.cr.services.User.Solicitud
import com.rayo.rayoxml.cr.services.Auth.LoginResponse
import com.rayo.rayoxml.cr.services.Auth.Prestamo
import com.rayo.rayoxml.cr.services.User.UserRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.services.User.UserViewModelFactory
import com.rayo.rayoxml.cr.ui.dialogs.BottomSheetDialog
import com.rayo.rayoxml.cr.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.cr.ui.slider_cards.CardAdapter
import com.rayo.rayoxml.utils.ContactoPyZ
import com.rayo.rayoxml.utils.PDFGenerator
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.utils.PrestamoPyZ
import com.rayo.rayoxml.utils.navigateToRenewalFragment
import com.rayo.rayoxml.cr.viewModels.RenewalViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rayo.rayoxml.CR.adapters.FinanceAdapter
import com.rayo.rayoxml.CR.adapters.createFinanceItems
import com.rayo.rayoxml.cr.viewModels.AuthViewModel
import com.rayo.rayoxml.cr.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.utils.EnvConfigCR
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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
    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: PreferencesManager
    private var userHasLoans: Boolean = false
    private var userHasActiveLoans: Boolean = true
    private var userContactId: String = ""
    private var userData: LoginResponse? = null

    // Modelo compartido renovaciones
    private val renewalViewModel: RenewalViewModel by activityViewModels()

    // Activar botones préstamos RP y PLP
    private var buttonRP: Boolean = false
    private var buttonPLP: Boolean = false

    // Tipos de préstamo
    private val PRESTAMO_MINI = "Préstamo mini"
    private val PRESTAMO_RAYO_PLUS = "Préstamo Rayo Plus"
    private val PRESTAMO_LARGO_PLAZO = "Préstamo Largo Plazo (PLP)"
    // Lista completa de préstamos
    val listaPrestamos: MutableList<Prestamo> = mutableListOf()

    private val PENDING_LOAN_STATUS = "Pendiente"
    private val COMPLETED_LOAN_STATUS = "Cancelado"
    private val REVISION_LOAN_STATUS = "En Revisión"
    private val COMPLETED_PAYMENT_STATUS = "Pagado"

    private val currencySymbol = "₡"

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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
//                findNavController().navigate(R.id.selectCountryFragment)
            }
        })
        setupViewPager()
        setupDotsIndicator()

        val scrollView = view.findViewById<ScrollView>(R.id.mainActivityScrollView)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.overScrollMode = View.OVER_SCROLL_NEVER

        preferencesManager = PreferencesManager(requireContext())
        val authFactory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

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
                    userContactId = user.id
                    userHasActiveLoans = hasActiveLoans(user.prestamos)
                    Log.d("HomeFragment CR", "Usuario con préstamos activos?: $userHasActiveLoans")

                    // Activar botones de préstamos
                    if(!userHasActiveLoans) binding.containerQuickAction.findViewById<ImageView>(R.id.MiniLock).visibility = View.GONE
                    // Préstamo RP
                    buttonRP = userData!!.DisponibleRPL == "1"
                    if(buttonRP) binding.containerQuickAction.findViewById<ImageView>(R.id.RpLock).visibility = View.GONE
                    buttonPLP = false
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

                    listaPrestamos.clear()
                    listaPrestamos.addAll(user.prestamos!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosRP!!.filterNotNull() ?: emptyList())
                    listaPrestamos.addAll(user.prestamosPLP!!.filterNotNull() ?: emptyList())

                    val firstLoan = listaPrestamos.get(0)
                    if (firstLoan != null) {
                        val loans = listaPrestamos ?: emptyList()
                        val cards = loans.map { loan ->
                            if (loan.pagos.isNotEmpty()) {
                                val firstLoanPayment = loan.pagos.get(0)
                                PrincipalLoanCardData(
                                    amount = "$currencySymbol${formatter.format(loan.montoPrestado.toDoubleOrNull())}",
                                    state = getLoanStatus(loan),
                                    payment = "Cuota: $currencySymbol${formatter.format(firstLoanPayment.montoPagar.toDouble())}",
                                    date = loan.fechaDeposito,
                                    paymentId = firstLoanPayment.id,
                                    loanId = loan.codigoPrestamo,
                                    loanType = loan.tipo,
                                    disbursementDate = "",
                                    code = loan.codigoPrestamo
                                )
                            }else if(loan.fechaDeposito == ""){
                                Log.d("HomeFragment CR", "Primer préstamo sin desembolsar")
                                PrincipalLoanCardData(
                                    amount = "$currencySymbol${formatter.format(loan.montoPrestado.toDoubleOrNull())}",
                                    state = getLoanStatus(loan),
                                    payment = "Cantidad de cuotas: ${getLoanPaymentsNumber(loan)}",
                                    date = "Fecha de desembolso: Pendiente",
                                    paymentId = loan.codigoPrestamo,
                                    loanId = loan.codigoPrestamo,
                                    loanType = loan.tipo,
                                    disbursementDate = "",
                                    code = loan.codigoPrestamo
                                )
                            }else{
                                Log.d("HomeFragment CR", "Primer préstamo sin pagos")
                                PrincipalLoanCardData(
                                    amount = "$currencySymbol${formatter.format(loan.montoPrestado.toDoubleOrNull())}",
                                    state = getLoanStatus(loan),
                                    payment = "Cuota:",
                                    date = loan.fechaDeposito,
                                    paymentId = loan.codigoPrestamo,
                                    loanId = loan.codigoPrestamo,
                                    loanType = loan.tipo,
                                    disbursementDate = "",
                                    code = loan.codigoPrestamo
                                )
                            }
                        }
                        principalCardLoanAdapter.setItems(cards)
                    } else {
                        Log.d("HomeFragment CR", "Primer préstamo no encontrado")
                    }

                } else {
                    Log.d("HomeFragment CR", "Usuario sin préstamos")
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
                "Línea de crédito hasta por \$2.500.000 COP desde tu préstamo N° 13"
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

        val financeAdapter = FinanceAdapter(createFinanceItems())
        binding.recyclerViewFinanceCards?.adapter = financeAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerViewFinanceCards)

        val adapter = CardAdapter(cards)
        recyclerView.adapter = adapter

        binding.buttonSeeText.setOnClickListener {
            goToPersonalLoan()
        }

        // Solicitar crédito
        binding.firstButtonLoanHome.setOnClickListener {
            renewalViewModel.setLoanType("MINI")
            navigateToRenewalFragment()
        }
        setupButtonListeners()
        setupQuickActionCards(view)

        // Obtener flag para actualizar datos
        val reloadUserData = arguments?.getBoolean("reloadUserData")
        Log.d("HomeFragment CR", "reloadUserData: $reloadUserData")
        if(reloadUserData == true){
            lifecycleScope.launch {
                delay(500)
                reloadUserData()
            }
        }
    }

    // Se requiere inicio de sesión para obtener datos de préstamos
    private fun reloadUserData(){
        Log.d("HomeFragment CR", "Recargando datos de usuario")

        try {
            lifecycleScope.launch {
                val user = authViewModel.user.firstOrNull()
                Log.d("HomeFragment CR", "Usuario: ${user}")
                if (user != null) {
                    Log.d("HomeFragment CR", "Usuario encontrado: ${user.id}")
                    userViewModel.getData(user.id)
                    binding.viewPagerPrincipalCards.invalidate()
                }else{
                    Log.d("HomeFragment CR", "Usuario no encontrado, buscando en userData")
                    if(userData != null){
                        Log.d("HomeFragment CR", "Usuario cargado: ${userData!!.id}")
                        userData!!.id?.let {
                            userViewModel.getData(it)
                            binding.viewPagerPrincipalCards.invalidate()
                        }
                    }else{
                        Log.d("HomeFragment CR", "Usuario userData no encontrado")
                    }
                }
            }
        }catch (e: Exception){
            println("❗ Exception: ${e.message}")
        }
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

        // recargar datos de usuario
        reloadUserData()

        val dialogView =
            layoutInflater.inflate(R.layout.bottom_sheet_dialog_options_installment, null)
        val dialog =
            com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
                setContentView(dialogView)
            }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewPayInstallment)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Controlar botones de opciones
        var disableOpenPaymentButton = false
        var disableViewPaymentButton = false
        var disablePdfPaymentButton = false

        when (item.state) {
            COMPLETED_LOAN_STATUS-> {
                disableOpenPaymentButton = true
            }
            REVISION_LOAN_STATUS -> {
                disableOpenPaymentButton = true
                disableViewPaymentButton = true
                disablePdfPaymentButton = true
            }
            else -> {
                disablePdfPaymentButton = true
            }
        }

        // Lista de opciones (solo texto)
        val items = listOf(
            OptionsInstallmentBottomAdapter.OptionWithIcon("Pagar cuota", R.drawable.ic_card, disableOption = disableOpenPaymentButton),
            OptionsInstallmentBottomAdapter.OptionWithIcon("Ver Pagos", R.drawable.ic_eye, disableOption = disableViewPaymentButton),
            //OptionsInstallmentBottomAdapter.OptionWithIcon("Paz y Salvo", R.drawable.ic_count_fees, disableOption = disablePdfPaymentButton)
        )

        // Configurar el adapter
        recyclerView.adapter = OptionsInstallmentBottomAdapter(items) { selectedOption ->
            // Manejar la opción seleccionada
            when (selectedOption) {
                "Pagar cuota" -> {
                    // Acción para "Pagar cuota"
                    // Inhabilitar navegación
                    //showPayInstallmentDetailsDialog(item)
                    // Url de pago directo
                    val url = EnvConfigCR.PAYMENT_URL
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }

                "Ver Pagos" -> {
                    // Acción para "Ver Pagos"
                    // Navegar al nuevo Fragment con el préstamo seleccionado
                    val _prestamo = listaPrestamos.find { it.codigoPrestamo == item.loanId }
                    if (_prestamo != null) {
                        //println("Préstamo encontrado: $_prestamo")
                        val bundle = Bundle().apply {
                            Log.d("PersonalLoanFragment", "Préstamo seleccionado: ${_prestamo}")
                            putParcelable("prestamoSeleccionado", _prestamo)
                        }
                        // Inhabilitar navegación
                        findNavController().navigate(
                            //R.id.action_personalLoanFragment_to_loanDetailFragment,
                            R.id.loanDetailFragment,
                            bundle,
                            NavOptions.Builder()
                                .build()
                        )
                    } else {
                        println("No se encontró un préstamo con ID $item.loanId")
                    }
                }

                /*"Paz y Salvo" -> {
                    //Log.d("HomeFragment CR", "ITEM: ${item}")
                    // Acción para "Paz y Salvo"
                    val contacto = ContactoPyZ(userData?.nombre!!, "", userData?.cedula!!)
                    val prestamo = PrestamoPyZ(item.loanId, item.code, item.date)
                    // Inhabilitar navegación
                    PDFGenerator.createAndDownloadPdf(requireContext(), contacto, prestamo)
                }*/
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
            Log.d("HomeFragment CR", "Seleccionado: " + selectedItem)
        } else {
            Log.d("HomeFragment CR", "Ninguno Seleccionado")
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
                    //R.id.radioBankTransfer -> "Botón Bancolombia"
                    else -> "" // Este caso no debería ocurrir porque ya verificamos que selectedId != -1
                }
                //Toast.makeText(requireContext(), "Pagando con: $selectedPaymentMethod", Toast.LENGTH_SHORT).show()
                if(selectedPaymentMethod == "Pagos Seguros en Linea"){
                    val url = EnvConfigCR.PAYMENT_URL
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }/*else {
                    val url = "https://rayocol--develop.sandbox.my.site.com/RecaudosRayoColombia/?idContacto=$userContactId&idPago=$paymentId"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }*/
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

        /*return if(userHasActiveLoans) false
        else true*/

        // Test
        if(userContactId == "003Ov00000TrJ8wIAF"){
            Log.d("HomeFragment", "Saltando restricción de usuario para renovar")
            return true
        }
        else if(userHasActiveLoans) return false
        else return true
        //return true
    }

    private fun hasActiveLoans(loans: List<Prestamo>): Boolean {
        return loans.any { prestamo ->
            prestamo.pagos.any { pago ->
                pago.estado != COMPLETED_PAYMENT_STATUS
            }
        }
    }

    private fun getLoanStatus(loan: Prestamo): String {
        return if (loan.pagos.any { it.estado != COMPLETED_PAYMENT_STATUS }) {
            PENDING_LOAN_STATUS
        } else {
            if(loan.fechaDeposito == "" && loan.pagos.isEmpty()) REVISION_LOAN_STATUS
            else COMPLETED_LOAN_STATUS
        }
    }

    private fun getLoanPaymentsNumber(loan: Prestamo): Int {
        val numberOfDates = listOf(loan.fecha1, loan.fecha2, loan.fecha3).count { !it.isNullOrBlank() }
        return numberOfDates
    }

}