package com.rayo.rayoxml.co.ui.home

import android.app.AlertDialog
import android.content.Intent
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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.OptionsInstallmentBottomAdapter
import com.rayo.rayoxml.co.adapters.PrincipalCardLoanAdapter
import com.rayo.rayoxml.databinding.FragmentHomeBinding
import com.rayo.rayoxml.databinding.FragmentPrincipalCardLoanBinding
import com.rayo.rayoxml.co.models.Card
import com.rayo.rayoxml.co.models.PrincipalLoanCardData
import com.rayo.rayoxml.co.services.Payment.PaymentLinkRequest
import com.rayo.rayoxml.co.services.Payment.PaymentRepository
import com.rayo.rayoxml.co.services.Payment.PaymentViewModel
import com.rayo.rayoxml.co.services.User.Prestamo
import com.rayo.rayoxml.co.services.User.Solicitud
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.ui.dialogs.BottomSheetDialog
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.co.ui.slider_cards.CardAdapter
import com.rayo.rayoxml.utils.ContactoPyZ
import com.rayo.rayoxml.utils.PDFGenerator
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.utils.PrestamoPyZ
import com.rayo.rayoxml.utils.navigateToRenewalFragment
import com.rayo.rayoxml.co.viewModels.RenewalViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import androidx.core.net.toUri
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearSnapHelper
import com.rayo.rayoxml.CO.adapters.FinanceAdapter
import com.rayo.rayoxml.CO.adapters.createFinanceItems
import com.rayo.rayoxml.co.services.Payment.CreatePaymentLinkRequest
import com.rayo.rayoxml.co.viewModels.AuthViewModel
import com.rayo.rayoxml.co.viewModels.AuthViewModelFactory
import com.rayo.rayoxml.utils.EnvConfigCO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardBinding: FragmentPrincipalCardLoanBinding
    private lateinit var principalCardLoanAdapter: PrincipalCardLoanAdapter

    private lateinit var preferencesManager: PreferencesManager

    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
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

    // Botón pagos
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var paymentViewModel: PaymentViewModel

    private val PAYMENT_URL_FOUND = "Link encontrado con éxito."
    private val PAYMENT_URL_NOT_FOUND = "No link generado o sin estado Created"
    private val PAYMENT_URL_REQUIRED = "Generar nuevo link"
    private val PAYMENT_URL_CREATED = "Link de pago generado con éxito."
    private val PAYMENT_URL_NOT_CREATED = "No se generó el link de pago. Inténtelo nuevamente."

    private val paymentPseUrl = EnvConfigCO.PAYMENT_URL

    private var CREATE_PAYMENT_LINK_TIMEOUT = 5000

    // Loading
    //private lateinit var paymentLoadingDialog: LoadingDialogFragment

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

        preferencesManager = PreferencesManager(requireContext())

        val authFactory = AuthViewModelFactory(preferencesManager)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        formatter.applyPattern("#,##0.00")  // Formato de moneda

        // Inicializar el modal
        //paymentLoadingDialog = LoadingDialogFragment()

        val scrollView = view.findViewById<ScrollView>(R.id.mainActivityScrollView)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.overScrollMode = View.OVER_SCROLL_NEVER

        // Recuperar el ViewModel compartido
        //val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

        // Loading
        val loadingDialog = LoadingDialogFragment()

        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        // Repositorio botón pagos
        paymentRepository = PaymentRepository()
        /*val paymentPreferencesManager = PreferencesManager(requireContext())
        val paymentFactory = PaymentViewModelFactory(paymentRepository, paymentPreferencesManager)
        paymentViewModel = ViewModelProvider(requireActivity(), paymentFactory)[PaymentViewModel::class.java]*/

        loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            Log.d("Observer", "User updated: $user")
            if (user != null) {
                userData = user
                if (user.prestamos?.isNotEmpty() == true) {
                    userHasLoans = true
                    userContactId = user.contacto!!
                    userHasActiveLoans = hasActiveLoans(user.prestamos)
                    Log.d("HomeFragment CO", "Usuario con préstamos activos: $userHasActiveLoans")

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
                                Log.d("HomeFragment CO", "Primer préstamo sin desembolsar")
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
                                Log.d("HomeFragment CO", "Primer préstamo sin pagos")
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
                        Log.d("HomeFragment CO", "Primer préstamo no encontrado")
                    }

                    loadingDialog.dismiss()

                } else {
                    Log.d("HomeFragment CO", "Usuario sin préstamos")
                    loadingDialog.dismiss()
                }
            }else{
                loadingDialog.dismiss()
            }
            //loadingDialog.dismiss() // Ocultar loading después de recibir respuesta

            // Botones de créditos activo/inactivo
            val hasActiveLoan = checkIfUserHasActiveLoan()

            if (hasActiveLoan) {
                cardBinding.cardLoanActive.visibility = View.VISIBLE
                binding.cardLoanInactive.visibility = View.GONE
                binding.containerQuickAction.visibility = View.VISIBLE

            } else {
                cardBinding.cardLoanActive.visibility = View.GONE
                binding.cardLoanInactive.visibility = View.VISIBLE
                binding.containerQuickAction.visibility = View.GONE
            }

            if (hasActiveLoan) {
                cardBinding.cardLoanActive.visibility = View.VISIBLE
                binding.cardLoanInactive.visibility = View.GONE
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
            Card(R.drawable.ic_bars_yellow, "Aumento de cupo en tus siguientes renovaciones"),
            Card(
                R.drawable.ic_money,
                "Línea de crédito hasta por \$2.500.000 COP desde tu préstamo N° 13"
            ),
            Card(R.drawable.ic_cupon, "Cupones de descuento desde el 8% al 20% en tus siguientes préstamos"),
            Card(
                R.drawable.ic_boletas,
                "Sorteos de boletas de cine. Bonos de consumo Electrodomésticos"
            )
        )

        val financeAdapter = FinanceAdapter(createFinanceItems()) // Debes crear este adaptador
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
            findNavController().navigate(R.id.loanFragment)
        }
        setupButtonListeners()
        setupQuickActionCards(view)

        // Obtener flag para actualizar datos
        val reloadUserData = arguments?.getBoolean("reloadUserData")
        Log.d("HomeFragment", "reloadUserData: $reloadUserData")
        if(reloadUserData == true){
            lifecycleScope.launch {
                delay(500)
                reloadUserData()
            }
        }
    }

    private fun reloadUserData(){
        Log.d("HomeFragment", "Recargando datos de usuario...")

        try {
            lifecycleScope.launch {
                val user = authViewModel.user.firstOrNull()
                Log.d("HomeFragment", "Usuario: ${user}")
                if (user != null) {
                    Log.d("HomeFragment", "Usuario encontrado: ${user.id}")
                    userViewModel.getData(user.id)
                    binding.viewPagerPrincipalCards.invalidate()
                    /*(requireActivity() as? AppCompatActivity)?.supportFragmentManager?.let {
                        it.beginTransaction()
                            .detach(this@HomeFragment)
                            .attach(this@HomeFragment)
                            .commitAllowingStateLoss()
                        userViewModel.getData(user.id) // vuelve a cargar los datos
                    }*/
                }else{
                    Log.d("HomeFragment", "Usuario no encontrado, buscando en userData")
                    if(userData != null){
                        Log.d("HomeFragment", "Usuario cargado: ${userData!!.contacto}")
                        userData!!.contacto?.let {
                            userViewModel.getData(it)
                            binding.viewPagerPrincipalCards.invalidate()
                        }
                        /*(requireActivity() as? AppCompatActivity)?.supportFragmentManager?.let {
                            it.beginTransaction()
                                .detach(this@HomeFragment)
                                .attach(this@HomeFragment)
                                .commitAllowingStateLoss()
                            userData!!.contacto?.let { it1 -> userViewModel.getData(it1) } // vuelve a cargar los datos
                        }*/
                    }else{
                        Log.d("HomeFragment", "Usuario userData no encontrado")
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
            "Cancelado" -> {
                disableOpenPaymentButton = true
            }
            "Pendiente Desembolso" -> {
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
            OptionsInstallmentBottomAdapter.OptionWithIcon("Paz y Salvo", R.drawable.ic_count_fees, disableOption = disablePdfPaymentButton)
        )

        /*val items = if (item.state == "Cancelado") {
            listOf(
                OptionsInstallmentBottomAdapter.OptionWithIcon("Pagar cuota", R.drawable.ic_card, true),
                OptionsInstallmentBottomAdapter.OptionWithIcon("Ver Pagos", R.drawable.ic_eye),
                OptionsInstallmentBottomAdapter.OptionWithIcon("Paz y Salvo", R.drawable.ic_count_fees)
            )
        } else {
            listOf(
                OptionsInstallmentBottomAdapter.OptionWithIcon("Pagar cuota", R.drawable.ic_card),
                OptionsInstallmentBottomAdapter.OptionWithIcon("Ver Pagos", R.drawable.ic_eye),
                OptionsInstallmentBottomAdapter.OptionWithIcon("Paz y Salvo", R.drawable.ic_count_fees, true)
            )
        }*/

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
                    // Navegar al nuevo Fragment con el préstamo seleccionado
                    val _prestamo = listaPrestamos.find { it.prestamoId == item.loanId }
                    if (_prestamo != null) {
                        //println("Préstamo encontrado: $_prestamo")
                        val bundle = Bundle().apply {
                            Log.d("PersonalLoanFragment", "Préstamo seleccionado: ${_prestamo}")
                            putParcelable("prestamoSeleccionado", _prestamo)
                        }
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

                    /*Toast.makeText(
                        requireContext(),
                        "Ver Pagos seleccionado para ${item.amount}",
                        Toast.LENGTH_SHORT
                    ).show()*/
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
        // recargar datos de usuario
        reloadUserData()

        val dialogView =
            layoutInflater.inflate(R.layout.bottom_sheet_dialog_payment_installment, null)
        val dialog =
            com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
                setContentView(dialogView)
            }
        // Configurar los datos en el diálogo
        val textType = dialogView.findViewById<TextView>(R.id.text_value_type)
        val textInstallment = dialogView.findViewById<TextView>(R.id.text_value_installment)
        val textInstallmentPending = dialogView.findViewById<TextView>(R.id.text_value_installment_pending)
        val textPay = dialogView.findViewById<TextView>(R.id.text_value_pay)
        val textDateInstallment = dialogView.findViewById<TextView>(R.id.text_value_date_installment)
        val textAmount = dialogView.findViewById<TextView>(R.id.text_value_amount)

        val _prestamo = listaPrestamos.find { it.prestamoId == item.loanId }
        if (_prestamo != null) {
            //println("Préstamo encontrado: $_prestamo")

            val primerPagoPendiente = listaPrestamos
                .mapNotNull { it.pagos }        // Descarta los null
                .flatten()                      // Aplana las listas válidas
                .find { it.estado == "Pendiente" }

            textType.text = _prestamo.tipo.replace("Préstamo ", "")
            textInstallment.text = _prestamo.numeroCuotas
            textInstallmentPending.text = _prestamo.cuotasPendientes
            if (primerPagoPendiente != null) {
                textPay.text = primerPagoPendiente.codigo
                textDateInstallment.text = primerPagoPendiente.fechaPago
                textAmount.text = "$${formatter.format(primerPagoPendiente.montoPagar.toDoubleOrNull())}"
            }else{
                textPay.text =""
                textDateInstallment.text = ""
                textAmount.text = ""
            }
        } else {
            println("No se encontró un préstamo con ID $item.loanId")
        }

        val selectedItem = item
        if (selectedItem != null) {
            Log.d("HomeFragment CO", "Seleccionado: " + selectedItem)
        } else {
            Log.d("HomeFragment CO", "Ninguno Seleccionado")
        }

        val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        val buttonAction = dialogView.findViewById<Button>(R.id.buttonAction)
        buttonAction.setOnClickListener {
            showPaymentMethodDialog(selectedItem.paymentId, selectedItem.loanType)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPaymentMethodDialog(paymentId: String, loanType: String) {
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
                    val url = "$paymentPseUrl?idContacto=$userContactId&idPago=$paymentId"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }else {
                    // Botón Bancolombia
                    getBankPaymentUrl(paymentId, loanType)
                }
                dialog.dismiss()
            } else {
                //Toast.makeText(requireContext(), "Selecciona una forma de pago", Toast.LENGTH_SHORT).show()
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

    private fun getBankPaymentUrl(paymentId: String, loanType: String, attempts: Int = 0, maxAttempts: Int = 5){
        //println("Intento $attempts")
        val paymentLoadingDialog = LoadingDialogFragment()
        paymentLoadingDialog.show(parentFragmentManager, "LoadingDialog")
        try {

            if (attempts >= maxAttempts) {
                println("Intentos máximos superados.")
                paymentLoadingDialog.dismiss()
                showErrorMessageDialog()
                return
            }

            var _loanType = ""

            when (loanType) {
                PRESTAMO_MINI -> _loanType = "mini"
                PRESTAMO_RAYO_PLUS -> _loanType = "plus"
                PRESTAMO_LARGO_PLAZO -> _loanType = "plp"
            }

            val request = PaymentLinkRequest(paymentId, _loanType)
            lifecycleScope.launch {
                val paymentUrlData = withContext(Dispatchers.IO) {
                    paymentRepository.getPaymentUrlData(request)
                }

                Log.d("HomeFragment", "Get payment url data: ${paymentUrlData}")

                if(paymentUrlData != null && paymentUrlData.solicitud.codigo == "200" && paymentUrlData.solicitud.result == PAYMENT_URL_FOUND){
                    paymentLoadingDialog.dismiss()
                    val paymentUrl = paymentUrlData.solicitud.linkPago
                    if (!paymentUrl.isNullOrEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, paymentUrl.toUri())
                        startActivity(intent)
                    }else{
                        Log.d("HomeFragment", "Link de pago inválido: $paymentUrl")
                    }
                }else if(paymentUrlData != null && paymentUrlData.solicitud.codigo == "300" &&
                    paymentUrlData.solicitud.result == PAYMENT_URL_NOT_FOUND && paymentUrlData.solicitud.accion == PAYMENT_URL_REQUIRED){
                    Log.d("HomeFragment", "Link de pago no creado")

                    val createPaymentUrlrequest = CreatePaymentLinkRequest(
                        idPago = paymentId,
                        metodoPago = "Bancolombia",
                        notificacionEmail = true,
                        notificacionWhatsapp = false,
                        tipoPrestamo = _loanType
                    )
                    val createPaymentUrlData = withContext(Dispatchers.IO) {
                        paymentRepository.createPaymentUrl(createPaymentUrlrequest)
                    }

                    Log.d("HomeFragment", "Create payment url request: ${createPaymentUrlrequest}")
                    Log.d("HomeFragment", "Create payment url data: ${createPaymentUrlData}")

                    if(createPaymentUrlData != null && createPaymentUrlData.solicitud.codigo == "200" && createPaymentUrlData.solicitud.result == PAYMENT_URL_CREATED){
                        // Obtener enlace
                        val recreatePaymentUrlData = withContext(Dispatchers.IO) {
                            paymentRepository.recreatePaymentUrl(createPaymentUrlrequest)
                        }
                        paymentLoadingDialog.dismiss()
                        Log.d("HomeFragment", "Recreate payment url request: ${createPaymentUrlrequest}")
                        Log.d("HomeFragment", "Recreate payment url data: ${recreatePaymentUrlData}")

                        if(recreatePaymentUrlData != null && recreatePaymentUrlData.solicitud.codigo == "200" && recreatePaymentUrlData.solicitud.result == PAYMENT_URL_CREATED){
                            val paymentUrl = recreatePaymentUrlData.solicitud.linkPago
                            if (!paymentUrl.isNullOrEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, paymentUrl.toUri())
                                startActivity(intent)
                            }else{
                                Log.d("HomeFragment", "Link de pago inválido: $paymentUrl")
                            }
                        }else{
                            // Error, Reintentar
                            //showErrorMessageDialog()
                            delay(CREATE_PAYMENT_LINK_TIMEOUT.toLong())
                            getBankPaymentUrl(paymentId, loanType, attempts+1, maxAttempts)
                        }
                    }
                    else if(createPaymentUrlData != null && createPaymentUrlData.solicitud.codigo == "300" && createPaymentUrlData.solicitud.result == PAYMENT_URL_NOT_CREATED){
                        paymentLoadingDialog.dismiss()
                        // Reintentar
                        //showErrorMessageDialog()
                        delay(CREATE_PAYMENT_LINK_TIMEOUT.toLong())
                        getBankPaymentUrl(paymentId, loanType, attempts+1, maxAttempts)
                    }else{
                        paymentLoadingDialog.dismiss()
                    }
                }else{
                    paymentLoadingDialog.dismiss()
                }
            }
        }catch (e: Exception){
            paymentLoadingDialog.dismiss()
            Log.e("HomeFragment", "Error obteniendo datos", e)
        }
    }

    private fun showErrorMessageDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Error al generar enlace")
            .setMessage("No fue posible generar el link de pago en este momento, por favor intente nuevamente o seleccione otro método de pago. También puede ponerse en contacto con uno de nuestros asesores para obtener asistencia")
            .setCancelable(false)
            .setPositiveButton("Cerrar") { _, _ ->

            }
            /*.setNegativeButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }*/
            .show()
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