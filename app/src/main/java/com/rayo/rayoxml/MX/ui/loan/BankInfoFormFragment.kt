package com.rayo.rayoxml.mx.ui.loan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.BankAdapter
import com.rayo.rayoxml.mx.adapters.CitiesAdapter
import com.rayo.rayoxml.mx.adapters.DepartmentAdapter
import com.rayo.rayoxml.mx.adapters.TypeAccountAdapter
import com.rayo.rayoxml.databinding.FragmentBankInfoFormBinding
import com.rayo.rayoxml.mx.models.loadBanksFromJson
import com.rayo.rayoxml.mx.models.loadDepartamentosFromJson
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepTwoRequest
import com.rayo.rayoxml.mx.services.Loan.LoanValidationRequest
import com.rayo.rayoxml.mx.services.Loan.Solicitud
import com.rayo.rayoxml.mx.services.Loan.Validacion
import com.rayo.rayoxml.mx.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.mx.ui.loading.VerificationDialogFragment
import com.rayo.rayoxml.mx.viewModels.FormViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModelFactory
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanValidationStepViewModelFactory
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.utils.NoPasteEditText
import com.rayo.rayoxml.utils.PreferencesManager


interface BankInfoNavigationListener {
    fun onNavigateToAddressInfo()
}

class BankInfoFormFragment(private val userParamsViewModel: UserViewModel) : Fragment() {

    private var _binding: FragmentBankInfoFormBinding? = null
    private val binding get() = _binding!!
    private val listaDepartamentos by lazy { loadDepartamentosFromJson(requireContext()) }
    private val loanRepository = LoanRepository()
    private var ciudadesDisponibles: List<String> = emptyList()
    private val banks  by lazy { loadBanksFromJson(requireContext()) }
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

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

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

        binding.formDepartment.addTextChangedListener { editable ->
            viewModel.departamento.value = editable?.toString()
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

        binding.formBank.setOnClickListener {
            showBankBottomSheet()
        }

        binding.formTypeAccount.setOnClickListener {
            showTypeAccountBottomSheet()
        }

        binding.formAddress.setOnClickListener {
            navigationListener?.onNavigateToAddressInfo()
        }
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

    private fun submitFormData(formData: LoanStepTwoRequest) {

        // Loading
        //val loadingDialog = LoadingDialogFragment()
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
                        val rejectBundle = Bundle().apply {
                            putSerializable("outcome", LoanOutcome.REJECTED)
                        }
                        findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
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
        val existingDialog = parentFragmentManager.findFragmentByTag("LoadingDialog") as? DialogFragment
        existingDialog?.dismissAllowingStateLoss()
    }

    // Validaciones formulario de préstamo
    private suspend fun loanFormValidations(formId: String, /*loadingDialog: LoadingDialogFragment,*/ attempts: Int = 0, maxAttempts: Int = 3){

        val rejectBundle = Bundle().apply {
            putSerializable("outcome", LoanOutcome.REJECTED)
        }

        if (attempts >= maxAttempts) {
            println("Max attempts reached. Exiting.")

            findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
            return
        }

        // Códigos de respuesta válidos para continuar
        val validCodes = arrayOf("200", "201", "966")

        val dismissTime = if (attempts == 0) 2000 else 7000 // 7s solo en reintentos
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
            /*if(solicitud?.codigo == "966"){
                solicitud = Validacion(
                    codigo = "200",
                    formulario = "a0GO4000009j5gnMAA",
                    propuesta = "512000",
                    step = "STEP4",
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
                            delay(7000) // Espera sin bloquear la UI
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

                findNavController().navigate(R.id.loanOutcomeFragment, rejectBundle)
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
            }
            "OTP" -> {
                // Cargar pantalla de OTP y validar código
                val dialog = VerificationDialogFragment()
                dialog.show(parentFragmentManager, "VerificationDialogFragment")

                // Pasar parámetros
                val bundle = Bundle().apply {
                    putString("option", "stepTwoFormId")
                    putString("formId", data.formulario)
                }
                (parentFragment as? FormFragment)?.goToNextStep(bundle)

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
/*    private fun showAddressDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar")
            .setMessage("¿Quieres ir a la información de dirección?")
            .setPositiveButton("Sí") { _, _ ->
                navigationListener?.onNavigateToAddressInfo()
            }
            .setNegativeButton("No", null)
            .show()
    }*/

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
/*        if (departmentSpinner.text.toString().trim().isEmpty() || departmentSpinner.text.toString().trim() == DEFAULT_SPINNER_DEPTO_SELECTED_OPTION) {
            errorDepartment.text = "Por favor, selecciona un departamento"
            errorDepartment.visibility = View.VISIBLE
            departmentSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            departmentSpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }*/

        // Validar ciudad
/*        if (citySpinner.text.toString().trim().isEmpty() || citySpinner.text.toString().trim() == DEFAULT_SPINNER_CITY_SELECTED_OPTION) {
            errorCity.text = "Por favor, selecciona una ciudad"
            errorCity.visibility = View.VISIBLE
            citySpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            citySpinner.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }*/

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

        // Validar número de cuenta
        /*if (numberAccountEditText.text.toString().trim().isEmpty()) {
            errorAccountNumber.text = "Por favor, ingresa un número de cuenta"
            errorAccountNumber.visibility = View.VISIBLE
            numberAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else if( !isValidBankReference(numberAccountEditText.text.toString().trim()) ){
            errorAccountNumber.text = "Debe tener mínimo 9 números, sin puntos o comas"
            errorAccountNumber.visibility = View.VISIBLE
            numberAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            numberAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar cuenta 2
        if (verifyAccountEditText.text.toString().trim().isEmpty()) {
            eerrorVerifyAccount.text = "Por favor, valida el número de cuenta"
            eerrorVerifyAccount.visibility = View.VISIBLE
            verifyAccountEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
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
                showError(errorAccountNumber, numberAccountEditText, "Debe tener mínimo 9 números, sin puntos o comas")
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

    fun isValidBankReference(reference: String): Boolean {
        val regex = Regex("^[0-9]{9,}$") // Debe tener al menos 9 dígitos numéricos
        return regex.matches(reference) &&
                !reference.contains("123456789") &&
                !reference.contains("00000000")
    }

}