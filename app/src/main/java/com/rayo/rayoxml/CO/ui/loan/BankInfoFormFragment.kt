package com.rayo.rayoxml.co.ui.loan

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.BankAdapter
import com.rayo.rayoxml.co.adapters.CitiesAdapter
import com.rayo.rayoxml.co.adapters.DepartmentAdapter
import com.rayo.rayoxml.co.adapters.TypeAccountAdapter
import com.rayo.rayoxml.databinding.FragmentBankInfoFormBinding
import com.rayo.rayoxml.co.models.loadBanksFromJson
import com.rayo.rayoxml.co.models.loadDepartamentosFromJson
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.co.services.Loan.LoanValidationRequest
import com.rayo.rayoxml.co.services.Loan.Solicitud
import com.rayo.rayoxml.co.services.Loan.Validacion
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.co.ui.loading.VerificationDialogFragment
import com.rayo.rayoxml.co.viewModels.FormViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.co.adapters.StepFragment
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.co.services.Loan.LoanStepOneViewModelFactory
import com.rayo.rayoxml.co.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.co.services.Loan.LoanValidationStepViewModelFactory
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.utils.NoPasteEditText
import com.rayo.rayoxml.utils.PreferencesManager

interface BankInfoNavigationListener {
    fun onNavigateToAddressInfo()
}

class BankInfoFormFragment(private val userParamsViewModel: UserViewModel) : Fragment(), StepFragment {
    override fun getStepTitle() = "Información Bancaria"
    private var _binding: FragmentBankInfoFormBinding? = null
    private val binding get() = _binding!!
    private val listaDepartamentos by lazy { loadDepartamentosFromJson(requireContext()) }
    private val loanRepository = LoanRepository()
    private var ciudadesDisponibles: List<String> = emptyList()
    private val banks  by lazy { loadBanksFromJson(requireContext()) }
    private val DEFAULT_SPINNER_DEPTO_SELECTED_OPTION = "Seleccione un departamento"
    private val DEFAULT_SPINNER_CITY_SELECTED_OPTION = "Seleccione una ciudad"
    private val DEFAULT_SPINNER_BANK_SELECTED_OPTION = "Seleccione su banco"
    private val DEFAULT_SPINNER_ACCOUNT_TYPE_SELECTED_OPTION = "Seleccione su tipo de cuenta"
    private var navigationListener: BankInfoNavigationListener? = null
    private lateinit var viewModel: FormViewModel

    // viewmodel de datos de usuario
    private lateinit var loanViewModel: LoanStepOneViewModel
    private lateinit var validationViewModel: LoanValidationStepViewModel
    private var formId: String? = null

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private var timeout = 10000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBankInfoFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showInfoAddressDialog()
        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        setupCityFieldState()

        // Obtener ViewModel compartido con la actividad
        val repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanStepOneViewModelFactory(repository, preferencesManager)
        loanViewModel = ViewModelProvider(requireActivity(), factory)[LoanStepOneViewModel::class.java]

        loanViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                formId = user.formulario
                Log.d("BankInfoFormFragment", "ID form ${formId}")
            }
        }

        viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

        // VierModel LoanValidationStep
        val validationsRepository = LoanRepository()
        val validationsPreferencesManager = PreferencesManager(requireContext())
        val validationsFactory = LoanValidationStepViewModelFactory(validationsRepository, validationsPreferencesManager)
        validationViewModel = ViewModelProvider(requireActivity(), validationsFactory)[LoanValidationStepViewModel::class.java]

        // Deshabilitar campo ciudad
        updateEditTextStyle(
            context = requireContext(),
            editText = binding.formCity,
            textColorResId = R.color.woodsmoke_500,
            backgroundColorResId = R.color.woodsmoke_200,
            enabled = false
        )

        // Cargar los datos guardados cuando el usuario regresa
        binding.formDepartment.setText(viewModel.departamento.value ?: "")
        binding.formCity.setText(viewModel.ciudadDepartamento.value ?: "")
        binding.formAddress.setText(viewModel.direccionExacta.value ?: "")
        binding.formBank.setText(viewModel.banco.value ?: "")
        binding.formTypeAccount.setText(viewModel.tipoCuenta.value ?: "")
        binding.formNumberAccount.setText(viewModel.referenciaBancaria.value ?: "")
        val formVerifyAccount = view.findViewById<NoPasteEditText>(R.id.form_verify_account)
        //binding.formVerifyAccount.setText(viewModel.referenciaBancaria2.value ?: "")
        formVerifyAccount.setText(viewModel.referenciaBancaria2.value ?: "")

        Log.d("Tipo", binding.formNumberAccount::class.java.name)

        binding.formDepartment.addTextChangedListener { editable ->
            viewModel.departamento.value = editable?.toString()
            // Habilitar campo ciudad
            updateEditTextStyle(
                context = requireContext(),
                editText = binding.formCity,
                textColorResId = R.color.woodsmoke_500,
                backgroundColorResId = R.color.pale_sky_50,
                enabled = true
            )
        }

        binding.formCity.addTextChangedListener { editable ->
            viewModel.ciudadDepartamento.value = editable?.toString()
        }

        binding.formAddress.addTextChangedListener { editable ->
            viewModel.direccionExacta.value = editable?.toString()
        }

        binding.formBank.addTextChangedListener { editable ->
            viewModel.banco.value = editable?.toString()
        }

        binding.formTypeAccount.addTextChangedListener { editable ->
            viewModel.tipoCuenta.value = editable?.toString()
        }

        binding.formNumberAccount.addTextChangedListener { editable ->
            viewModel.referenciaBancaria.value = editable?.toString()
        }

        /*binding.*/formVerifyAccount.addTextChangedListener { editable ->
            viewModel.referenciaBancaria2.value = editable?.toString()
        }

        setFragmentResultListener("requestKeyAddress") { _, bundle ->
            val address = bundle.getString("addressKey") ?: ""
            binding.formAddress.setText(address) // Mostrar la dirección
        }

        viewModel.via.observe(viewLifecycleOwner, { via ->
            binding.formAddress.setText("$via ${viewModel.number.value} ${viewModel.carrer.value} ${viewModel.complement.value} ${viewModel.detail.value}")
        })

        val via = arguments?.getString("via") ?: ""
        val number = arguments?.getString("number") ?: ""
        val carrer = arguments?.getString("carrer") ?: ""
        val complement = arguments?.getString("complement") ?: ""
        val detail = arguments?.getString("detail") ?: ""

        val addressEditText = view.findViewById<EditText>(R.id.form_address)
        addressEditText.setText("$via $number $carrer $complement $detail")

        binding.btnNextStep.setOnClickListener {
            if (validateForm()) {

                val formData = collectFormData()

                Log.d("BankInfoFormFragment", formData.toString())

                submitFormData(formData)

                // Ocultar fragment de OTP
                //val dialog = VerificationDialogFragment()
                //dialog.show(parentFragmentManager, "VerificationDialogFragment")
            } else {
                //Toast.makeText(requireContext(), "Corrige los errores antes de continuar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.formDepartment.setOnClickListener {
            showDepartamentoBottomSheet()
        }

        binding.formCity.setOnClickListener {
            if (ciudadesDisponibles.isNotEmpty()) {
                showCityBottomSheet(ciudadesDisponibles)
            } else {
                //Toast.makeText(requireContext(), "Selecciona un departamento primero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.formBank.setSingleClickListener {
            showBankBottomSheet()
        }

        binding.formTypeAccount.setSingleClickListener {
            showTypeAccountBottomSheet()
        }

        binding.formAddress.setSingleClickListener {
            navigationListener?.onNavigateToAddressInfo()
        }

        // Bloquear opción de pagar en campos de cuenta
        disableAccountNumberPaste()
    }

    fun updateEditTextStyle(
        context: Context,
        editText: EditText,
        textColorResId: Int,
        backgroundColorResId: Int,
        enabled: Boolean
        //borderColorResId: Int
    ) {
        // Cambiar color de texto
        editText.setTextColor(ContextCompat.getColor(context, textColorResId))

        // Cambiar color fondo
        //editText.background.setTint(ContextCompat.getColor(context, backgroundColorResId))
        val drawable = ContextCompat.getDrawable(context, R.drawable.bg_edit_text)?.mutate()
        val gradient = (drawable as? StateListDrawable)?.current as? GradientDrawable

        gradient?.setColor(ContextCompat.getColor(context, backgroundColorResId)) // fondo
        //gradient?.setStroke(2, ContextCompat.getColor(context, borderColorResId)) // borde

        editText.background = drawable
        editText.isEnabled = enabled
    }

    fun View.setSingleClickListener(delay: Long = 600, action: () -> Unit) {
        var lastClickTime = 0L
        setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < delay) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            action()
        }
    }

    private fun disableAccountNumberPaste(){
        val numberAccountEditText = binding.formNumberAccount
        //val verifyAccountEditText = binding.formVerifyAccount
        val verifyAccountEditText = requireView().findViewById<NoPasteEditText>(R.id.form_verify_account)

        // evitar el menú emergente de pegar al mantener presionado
        numberAccountEditText.setOnLongClickListener { true } // Disable long press
        numberAccountEditText.setLongClickable(false)
        numberAccountEditText.isLongClickable = false
        numberAccountEditText.setTextIsSelectable(false)

        verifyAccountEditText.setOnLongClickListener { true } // Disable long press
        verifyAccountEditText.setLongClickable(false)
        verifyAccountEditText.isLongClickable = false
        verifyAccountEditText.setTextIsSelectable(false)

        // Desactiva la selección y el menú contextual (copiar, pegar, cortar)
        numberAccountEditText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        }
        verifyAccountEditText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        // Ocultar clipboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            numberAccountEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)
            numberAccountEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)

            verifyAccountEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD)
            verifyAccountEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)
        }

        // Para API 11+
        /*numberAccountEditText.setOnTouchListener { v, event ->
            v.performClick() // para accesibilidad
            (v as? EditText)?.clearFocus()
            true
        }
        verifyAccountEditText.setOnTouchListener { v, event ->
            v.performClick() // para accesibilidad
            (v as? EditText)?.clearFocus()
            true
        }*/
    }

    private fun showDepartamentoBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_departamentos, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerDepartamentos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = DepartmentAdapter(listaDepartamentos.map { it.departamento }) { selectedDepartamentoName ->
            binding.formDepartment.setText(selectedDepartamentoName)
            val selectedDepartamento = listaDepartamentos.find { it.departamento == selectedDepartamentoName }
            ciudadesDisponibles = selectedDepartamento?.municipios ?: emptyList()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCityBottomSheet(ciudades: List<String>) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_cities, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCities)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = CitiesAdapter(ciudades) { selectedCity ->
            binding.formCity.setText(selectedCity)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBankBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_bank, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewBanks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = BankAdapter(banks.map { it.name }) { selectedBankName ->
            binding.formBank.setText(selectedBankName)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showTypeAccountBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_type_account, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewTypeAccount)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TypeAccountAdapter { selectedType ->
            binding.formTypeAccount.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun gotoRejectScreen(){

        resetFormFields(binding.root) // Borrar datos formulario

        val rejectBundle = Bundle().apply {
            putSerializable("outcome", LoanOutcome.REJECTED)
        }
        findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
    }

    private fun collectFormData(): LoanStepTwoRequest {

        val departmentSpinner = binding.formDepartment
        val citySpinner = binding.formCity
        val addressEditText = binding.root.findViewById<EditText>(R.id.form_address)
        val bankSpinner = binding.formBank
        val typeAccountSpinner = binding.formTypeAccount
        val numberAccountEditText = binding.root.findViewById<EditText>(R.id.form_number_account)
        val verifyAccountEditText = binding.root.findViewById<EditText>(R.id.form_verify_account)

        val selectedAccountType = binding.formTypeAccount.text.toString().trim()
        var fullAccountType = ""
        if(selectedAccountType == "Ahorros"){
            fullAccountType = "Cuenta de Ahorros"
        }else if(selectedAccountType == "Corriente"){
            fullAccountType = "Cuenta Corriente"
        }
        return LoanStepTwoRequest(
            formulario = formId.toString(), // traer de la respuesta de la API del paso 1
            departamento = binding.formDepartment.text.toString().trim(),
            ciudadDepartamento = binding.formCity.text.toString().trim(),
            direccionExacta = addressEditText.text.toString().trim(),
            banco = binding.formBank.text.toString().trim(),
            tipoCuenta = fullAccountType,
            referenciaBancaria = numberAccountEditText.text.toString().trim(),
            referenciaBancaria2 = verifyAccountEditText.text.toString().trim(),
            plazoSeleccionado = 30, // traer de simulador (días)
            telefonoEmpresa = "00000000",
            contadorActualizado = 1, // Revisar de donde se obtiene
            showModal = false,
            checkTecnologia = false
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is BankInfoNavigationListener) {
            navigationListener = parentFragment as BankInfoNavigationListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }

    private fun setupCityFieldState() {
        // Inicialmente deshabilitar el campo de ciudad
        disableCityField()

        // Observar cambios en el campo de departamento
        binding.formDepartment.addTextChangedListener { editable ->
            if (editable.isNullOrEmpty()) {
                disableCityField()
            } else {
                enableCityField()
                // Limpiar ciudad seleccionada si cambian el departamento
                binding.formCity.text?.clear()
            }
        }
    }

    private fun disableCityField() {
        binding.formCity.apply {
            isEnabled = false
            isClickable = false
            alpha = 0.5f
            hint = "Seleccione primero un departamento"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_300))
        }
    }

    private fun enableCityField() {
        binding.formCity.apply {
            isEnabled = true
            isClickable = true
            alpha = 1f
            hint = "Seleccione una ciudad"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_700))
        }
    }

    private fun submitFormData(formData: LoanStepTwoRequest) {

        //Si se quiere poner el texto solo es agregar esta linea de codigo
        val loadingDialog = LoadingDialogFragment.newInstance(true)

        loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

        lifecycleScope.launch {
            try {
                val loanStepTwoResponse = withContext(Dispatchers.IO) {
                    loanRepository.getDataStepTwo(formData) // Llamada suspend
                }

                loadingDialog.dismiss() // Ocultar loading después de recibir respuesta

                // AJUSTES PARA TEST
                //(parentFragment as? FormFragment)?.setFormId("a0GO4000009Ch8rMAC")
                var solicitud: Solicitud? = loanStepTwoResponse?.solicitud
                /*if(solicitud?.codigo !== "200"){
                    solicitud = Solicitud(
                        codigo = "200",
                        formulario = "a0GO4000009VowEMAS",
                        result = "La solicitud fue procesada exitosamente"
                    )
                }*/

                Log.d("BankInfoFormFragment", "Loan step two: ${loanStepTwoResponse}")
                if (solicitud != null) {
                    if(solicitud.formulario?.trim() !== null){
                        val formId = solicitud.formulario?.trim()
                        //Toast.makeText(requireContext(), "Id formulario: ${formId}", Toast.LENGTH_SHORT).show()

                        // Asignar datos bancarios a viewModel
                        userParamsViewModel.setBankData(formData)

                        // Servicio de validaciones
                        if (formId != null) {
                            loanFormValidations(formId /*, loadingDialog*/)
                        }

                        // Pasar parámetros
                        /*val bundle = Bundle().apply {
                                    putString("option", "stepTwoFormId")
                                    putString("formId", solicitud.formulario)
                                }*/
                        // No mostrar validación OTP en este punto
                        //(parentFragment as? FormFragment)?.goToNextStep(bundle)
                    }else{
                        Log.d("BankInfoFormFragment", "Id de formulario inválido")
                        gotoRejectScreen()
                    }
                }
                else Log.d("BankInfoFormFragment", "Solicitud nula")
            } catch (e: Exception) {
                loadingDialog.dismiss() // Ocultar en caso de error
                Log.e("BankInfoFormFragment", "Error obteniendo datos", e)
            }
        }
        //Toast.makeText(requireContext(), "Formulario completado y enviado", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Función para cerrar el modal de manera segura
    fun dismissLoadingDialog() {
        if (isAdded){
            val existingDialog = parentFragmentManager.findFragmentByTag("LoadingDialog") as? DialogFragment
            existingDialog?.dismissAllowingStateLoss()
        }
    }

    // Validaciones formulario de préstamo
    private suspend fun loanFormValidations(formId: String, /*loadingDialog: LoadingDialogFragment,*/ attempts: Int = 0, maxAttempts: Int = 3){

        if (attempts >= maxAttempts) {
            println("Max attempts reached. Exiting.")
            gotoRejectScreen()
            return
        }

        // Códigos de respuesta válidos para continuar
        val validCodes = arrayOf("200", "201", "966")

        val dismissTime = if (attempts == 0) 2000 else timeout // 10s solo en reintentos
        val loadingDialog = LoadingDialogFragment(dismissTime)

        // Mostrar el modal solo si no está ya en pantalla
        if (!loadingDialog.isAdded) {
            withContext(Dispatchers.Main) {
                loadingDialog.show(parentFragmentManager, "LoadingDialog")
            }
        }

        //loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

        try {
            val formData = LoanValidationRequest(formId)
            val loanValidationResponse = withContext(Dispatchers.IO) {
                loanRepository.getDataValidationStep(formData)
            }

            // Actualizar datos para usar en fragment LoanProposal
            if (loanValidationResponse != null) {
                validationViewModel.setData(loanValidationResponse)
            }
            //loadingDialog.dismiss()
            //dismissLoadingDialog()

            var solicitud: Validacion? = loanValidationResponse?.solicitud

            // AJUSTES PARA TEST
            /*if(solicitud?.codigo != "200"){
                solicitud = Validacion(
                    codigo = "200",
                    formulario = "a0GO4000009j5gnMAA",
                    propuesta = "512000",
                    step = "OTP",
                    scoreExperian = "500",
                    result = "La solicitud fue procesada exitosamente"
                )
            }*/

            if(solicitud != null && solicitud.codigo in validCodes){
                // Valor de STEP -> STEP4 = continuar, OTP = Validación tel
                Log.d("BankInfoFormFragment", "Resultado validación de formualario: ${solicitud}")
                ////Toast.makeText(requireContext(), "Resultado validación de formualario: ${solicitud.result}", Toast.LENGTH_LONG).show()

                when (solicitud.codigo) {
                    "200", "201" -> {
                        dismissLoadingDialog() // Se cierra inmediatamente
                        // Pasar a la propuesta
                        continuarPropuesta(solicitud)
                    }
                    "966" -> {
                        // Repetir llamado a servicio luego de 7 segundos
                        //val newloadingDialog = LoadingDialogFragment()

                        // Mantener el modal en pantalla 7 segundos ANTES de ejecutar la siguiente acción
                        lifecycleScope.launch {
                            delay(timeout.toLong()) // Espera sin bloquear la UI
                            dismissLoadingDialog()
                            loanFormValidations(formId, attempts + 1, maxAttempts) // Llamada recursiva con contador
                        }
                    }
                    else -> {
                        dismissLoadingDialog() // Se cierra inmediatamente
                    }
                }
            }else{
                dismissLoadingDialog() // Se cierra inmediatamente en caso de respuesta inválida
                // Mostrar pantalla de rechazo
                Log.d("BankInfoFormFragment", "Error en validación de formualario: ${loanValidationResponse?.solicitud}")
                //Toast.makeText(requireContext(), "Error en validación de formualario: ${loanValidationResponse?.solicitud?.result}", Toast.LENGTH_LONG).show()

                gotoRejectScreen()
                return
            }
        } catch (e: Exception) {
            //loadingDialog.dismiss() // Ocultar en caso de error
            dismissLoadingDialog()
            Log.e("BankInfoFormFragment", "Error obteniendo datos", e)
        }
    }

    private fun continuarPropuesta(data: Validacion){
        when (data.step) {
            "STEP4" -> {
                // Obtener datos de propuesta
                val propuesta = data.propuesta
                // Continuar con STEP4

                // Pasar parámetros
                val bundle = Bundle().apply {
                    putString("option", "proposal")
                    putString("proposalValue", data.propuesta)
                }
                (parentFragment as? FormFragment)?.goToNextStep(bundle, false)
                resetFormFields(binding.root) // Borrar datos formulario
            }
            "OTP" -> {
                Log.d("BankInfoFormFragment", "Cargando OTP")
                // Cargar pantalla de OTP y validar código
                val dialog = VerificationDialogFragment()
                dialog.show(parentFragmentManager, "VerificationDialogFragment")

                // Pasar parámetros
                val bundle = Bundle().apply {
                    putString("option", "stepTwoFormId")
                    putString("formId", data.formulario)
                }
                (parentFragment as? FormFragment)?.goToNextStep(bundle)
                resetFormFields(binding.root) // Borrar datos formulario

                // implementar servicio
                val OtpValidationResultCode = 200
                val OtpValidationResult = "VALIDO"

                if(OtpValidationResultCode == 200){
                    // Validar respuesta
                    when (OtpValidationResult) {
                        "VALIDO" -> {
                            // Continuar con STEP4
                            //
                        }
                        else -> {
                            // Error OTP
                            // Mostrar error en pantalla
                        }
                    }

                }else{
                    // Redireccionar a pantalla de preguntas

                    // implementar servicio
                    //data.Solicitud.RESULTADOCUESTIONARIO = "VALIDO"
                    val QuestionsValidationResult = "VALIDO"

                    if(QuestionsValidationResult == "VALIDO"){
                        // Continuar con STEP4
                        //
                    }else{
                        // Mostrar pantalla de rechazo
                        //
                    }
                }
            }
            else -> {
                // Otros estados
            }
        }
    }

    private fun showInfoAddressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_bank_info, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogView.findViewById<TextView>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun resetFormFields(container: ViewGroup) {
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            when (view) {
                is EditText -> view.text?.clear()
                is CheckBox -> view.isChecked = false
                is Spinner -> view.setSelection(0)
                is ViewGroup -> resetFormFields(view) // recursive for nested groups
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val departmentSpinner = binding.formDepartment
        val citySpinner = binding.formCity
        val addressEditText = binding.root.findViewById<EditText>(R.id.form_address)
        val bankSpinner = binding.formBank
        val typeAccountSpinner = binding.formTypeAccount
        val numberAccountEditText = binding.root.findViewById<EditText>(R.id.form_number_account)
        val verifyAccountEditText = binding.root.findViewById<EditText>(R.id.form_verify_account)

        val errorDepartment = binding.errorDepartment
        val errorCity = binding.errorCity
        val errorAddress = binding.errorAddress
        val errorBank = binding.errorBank
        val errorTypeAccount = binding.errorTypeAccount
        val errorAccountNumber = binding.errorAccountNumber
        val eerrorVerifyAccount = binding.errorVerifyAccount

        // Ocultar todos los mensajes de error al inicio
        errorDepartment.visibility = View.GONE
        errorCity.visibility = View.GONE
        errorAddress.visibility = View.GONE
        errorBank.visibility = View.GONE
        errorTypeAccount.visibility = View.GONE
        errorAccountNumber.visibility = View.GONE
        eerrorVerifyAccount.visibility = View.GONE

        // Validar departamento
        if (departmentSpinner.text.toString().trim().isEmpty() || departmentSpinner.text.toString().trim() == DEFAULT_SPINNER_DEPTO_SELECTED_OPTION) {
            errorDepartment.text = "Por favor, selecciona un departamento"
            errorDepartment.visibility = View.VISIBLE
            departmentSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            departmentSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar ciudad
        if (citySpinner.text.toString().trim().isEmpty() || citySpinner.text.toString().trim() == DEFAULT_SPINNER_CITY_SELECTED_OPTION) {
            errorCity.text = "Por favor, selecciona una ciudad"
            errorCity.visibility = View.VISIBLE
            citySpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            citySpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar dirección
        if (addressEditText.text.toString().trim().isEmpty()) {
            errorAddress.text = "Por favor, ingresa una dirección"
            errorAddress.visibility = View.VISIBLE
            addressEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            addressEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar banco
        if (bankSpinner.text.toString().trim().isEmpty() || bankSpinner.text.toString().trim() == DEFAULT_SPINNER_BANK_SELECTED_OPTION) {
            errorBank.text = "Por favor, selecciona un banco"
            errorBank.visibility = View.VISIBLE
            bankSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            bankSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar tipo de cuenta
        if (typeAccountSpinner.text.toString().trim().isEmpty() || typeAccountSpinner.text.toString().trim() == DEFAULT_SPINNER_ACCOUNT_TYPE_SELECTED_OPTION) {
            errorTypeAccount.text = "Por favor, selecciona un tipo de cuenta"
            errorTypeAccount.visibility = View.VISIBLE
            typeAccountSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            typeAccountSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

/*// Validar número de cuenta
        val account = numberAccountEditText.text.toString().trim()
        if (account.isEmpty()) {
            errorAccountNumber.text = "Por favor, ingresa un número de cuenta"
            errorAccountNumber.visibility = View.VISIBLE
            numberAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else if (!isValidBankReference(account)) {
            errorAccountNumber.text = "Debe tener al menos 12 números, sin puntos ni comas, ni secuencias inválidas"
            errorAccountNumber.visibility = View.VISIBLE
            numberAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            errorAccountNumber.visibility = View.GONE
            numberAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

// Validar cuenta 2
        val verifyAccount = verifyAccountEditText.text.toString().trim()
        if (verifyAccount.isEmpty()) {
            eerrorVerifyAccount.text = "Por favor, valida el número de cuenta"
            eerrorVerifyAccount.visibility = View.VISIBLE
            verifyAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            eerrorVerifyAccount.visibility = View.GONE
            verifyAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }*/

        val isValidAccount: Boolean = validateAccountNumber()
        if( !isValidAccount ) isValid = false

        return isValid
    }

    // Validar número de cuenta bancaria
    fun validateAccountNumber(): Boolean {

        val numberAccountEditText = binding.root.findViewById<EditText>(R.id.form_number_account)
        val verifyAccountEditText = binding.root.findViewById<EditText>(R.id.form_verify_account)

        val errorAccountNumber = binding.errorAccountNumber
        val errorVerifyAccount = binding.errorVerifyAccount

        val accountNumber = numberAccountEditText.text.toString().trim()
        val confirmAccountNumber = verifyAccountEditText.text.toString().trim()

        when {
            accountNumber.isEmpty() -> {
                showError(errorAccountNumber, numberAccountEditText, "Por favor, ingresa el número de cuenta")
                return false
            }
            !isValidBankReference(accountNumber) -> {
                showError(errorAccountNumber, numberAccountEditText, "Debe tener mínimo 12 números, sin puntos o comas")
                return false
            }
            confirmAccountNumber.isEmpty() -> {
                showError(errorVerifyAccount, verifyAccountEditText, "Por favor, confirma tu número de cuenta")
                return false
            }
            accountNumber != confirmAccountNumber -> {
                showError(errorVerifyAccount, verifyAccountEditText, "Los números de cuenta no coinciden")
                return false
            }
            else -> {
                hideError(errorAccountNumber, numberAccountEditText)
                hideError(errorVerifyAccount, verifyAccountEditText)
                return true
            }
        }
    }

    private fun showError(errorTextView: TextView, editText: EditText, message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
    }

    private fun hideError(errorTextView: TextView, editText: EditText) {
        errorTextView.visibility = View.GONE
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
    }

    private fun isValidBankReference(account: String): Boolean {
        val onlyNumbersPattern = Regex("^[0-9]{12,}$")
        val noSequential123456789 = Regex("^(?!.*123456789).*$")
        val noRepeatedZeros = Regex("^(?!.*00000000).*$")

        return account.matches(onlyNumbersPattern)
                && account.matches(noSequential123456789)
                && account.matches(noRepeatedZeros)
    }
}