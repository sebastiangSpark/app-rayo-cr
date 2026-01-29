package com.rayo.rayoxml.mx.ui.loan

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentPersonalInfoFormBinding
import com.rayo.rayoxml.mx.ui.dialogs.DatePickerBottomSheetDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.mx.services.Loan.LexisNexisHelper
import com.rayo.rayoxml.mx.services.Loan.LoanRepository
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneRequest
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModel
import com.rayo.rayoxml.mx.services.Loan.LoanStepOneViewModelFactory
import com.rayo.rayoxml.mx.services.User.UserRepository
import com.rayo.rayoxml.mx.services.User.UserViewModel
import com.rayo.rayoxml.mx.services.User.UserViewModelFactory
import com.rayo.rayoxml.mx.ui.dialogs.HaveAccountBottomSheetDialog
import com.rayo.rayoxml.mx.ui.dialogs.UserEmployedBottomSheetDialog
import com.rayo.rayoxml.mx.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.mx.ui.loan.outcome.LoanOutcome
import com.rayo.rayoxml.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.rayo.rayoxml.mx.services.Loan.Solicitud as Solicitud
import com.rayo.rayoxml.mx.services.User.Solicitud as SolicitudUser

class PersonalInfoFormFragment(private val userParamsViewModel: UserViewModel) : Fragment() {

    private var _binding: FragmentPersonalInfoFormBinding? = null
    private val binding get() = _binding!!
    /*private val viewModelfull: FormViewModel by activityViewModels()*/

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private lateinit var request: LoanStepOneRequest

    private val loanRepository = LoanRepository()

    private lateinit var viewModel: LoanStepOneViewModel

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    // datos de usuario logueado
    private var userData: SolicitudUser? = null
    private var registeredUser: Boolean = false
    private var userContactId: String = ""
    private var loanTerm: Int = 0
    private var loanAmount: Int = 0

    private val helper = LexisNexisHelper()

    private val DEFAULT_SPINNER_SELECTED_OPTION = "Seleccione"

    // Mensajes de error para cuenta nueva
    val duplicateIdDocumentCode = "405"
    val duplicateIdDocumentMessage = "El contacto ya se encuentra creado en Salesforce"
    val duplicatePhoneNumberCode = "499"
    val duplicatePhoneNumberMessage = "El celular ya se encuentra registrado."
    val duplicateEmailCode = "499"
    val duplicateEmailMessage = "El correo ya se encuentra registrado."

    // Mensajes a mostrar en pantalla para cuenta nueva
    val idDocumentErrorMessage = "El documento de identidad proporcionado ya se encuentra registrado."
    val phoneNumberErrorMessage = "El celular ya se encuentra registrado."
    val emailErrorMessage = "El correo ya se encuentra registrado."

    // viewmodel de datos de usuario
    private lateinit var loanViewModel: LoanStepOneViewModel

    // Habilitar carga de datos
    private var canContinue = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.formUserHaveAccount.setOnClickListener {
            showBottomSheetDialogBank()
        }

        binding.formUserEmploye.setOnClickListener {
            showBottomSheetDialogUser()
        }

        binding.textViewDataTreatment.setOnClickListener {
            showDataTreatmentDialog()
        }

        // Asignar OnClickListener al TextView de "términos y condiciones"
        binding.textViewTermsAndConditions.setOnClickListener {
            showTermsAndConditionsDialog()
        }

        setupListeners()
        // Cargar datos de usuario si hay sesión
        loadLoggedInUserData()

        // Obtener ViewModel compartido con la actividad
        val repository = LoanRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = LoanStepOneViewModelFactory(repository, preferencesManager)
        loanViewModel = ViewModelProvider(requireActivity(), factory)[LoanStepOneViewModel::class.java]

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()
    }

    private fun processData(solicitud: Solicitud){

        //loanViewModel.clearUserData() // Limpiar datos después de usarlos

        //loadingDialog.dismiss() // Ocultar loading después de recibir respuesta
        //dismissLoadingDialog()

        // Aquí puedes actualizar la UI con la respuesta
        Log.d("PersonalInfoFormFragment", "Loan step one: ${solicitud}")

        if(solicitud.codigo.trim() == "200"){
            if( solicitud.formulario?.trim() != null){
                //Toast.makeText(requireContext(), "Id formulario: ${loanStepOneResponse.solicitud.formulario}", Toast.LENGTH_SHORT).show()
                // Pasar parámetros
                /*val bundle = Bundle().apply {
                    putString("option", "stepOneFormId")
                    putString("formId", loanStepOneResponse.solicitud.formulario)
                }*/
                val bundle = Bundle()
                bundle.putString("option", "stepOneFormId")
                bundle.putString("formId", solicitud.formulario)
                // Asignar id de formulario
                parentFragmentManager.setFragmentResult("personalInfoFragmentKey", bundle)
                //(parentFragment as? FormFragment)?.setFormId(loanStepOneResponse.solicitud.formulario)

                //Asignar datos para formulario
                userParamsViewModel.setFormId(solicitud.formulario!!)
                userParamsViewModel.setPersonalData(request)

                (parentFragment as? FormFragment)?.goToNextStep(bundle)
            }else{
                Log.d("PersonalInfoFormFragment", "Id de formulario inválido")
                // Obtener errores en datos de cuenta
                //showAccountErrors(loanStepOneResponse?.solicitud!!)
            }
        }
        else if(solicitud.codigo?.trim() == "600"){
            // No cumple requisitos
            val bundle = Bundle().apply {
                putSerializable("outcome", LoanOutcome.REJECTED)
            }
            findNavController().navigate(R.id.loanOutcomeFragment, bundle)
        }
        else {
            Log.d("PersonalInfoFormFragment", "Error: ${solicitud}")
            // Obtener errores en datos de cuenta
            showAccountErrors(solicitud)
        }
    }

    // Función para cerrar el modal de manera segura
    fun dismissLoadingDialog() {
        val existingDialog = parentFragmentManager.findFragmentByTag("LoadingDialog") as? DialogFragment
        existingDialog?.dismissAllowingStateLoss()
        /*if (isAdded && !isRemoving && !isDetached) {
            val existingDialog = parentFragmentManager.findFragmentByTag("LoadingDialog") as? DialogFragment
            existingDialog?.dismissAllowingStateLoss()
        }*/
    }

    private fun loadLoggedInUserData(){
        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        userViewModel.personalData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                loanTerm = data.plazoSeleccionado
                loanAmount = data.valorSeleccionado

                Log.d("PersonalInfoFormFragment", "Plazo obtenido simulador: ${loanTerm}")
                Log.d("PersonalInfoFormFragment", "Monto obtenido simulador: ${loanAmount}")
            }
        }

        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                userData = user
                registeredUser = true
                userContactId = user.contacto!!

                //Asignar datos para tumipay
                userParamsViewModel.setTumiPay(user.mostrarTumipay!!)

                // Cargar información
                binding.formUserName.setText(user.nombre)
                binding.formUserLastname.setText(user.apellidos)
                binding.formUserDocument.setText(user.documento)
                binding.formUserPhone.setText(user.celular)
                binding.formUserEmail.setText(user.email)

                // Ocultar campos no requeridos
                binding.userPasswordTextView.visibility = View.GONE
                binding.passwordTextInputLayout.visibility = View.GONE
                binding.formPasswordEditText.visibility = View.GONE

                binding.userValidateEmailTextView.visibility = View.GONE
                binding.formUserValidateEmail.visibility = View.GONE

                // deshabilitar campos principales
                val backgroundColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_300)

                binding.formUserName.isEnabled = false
                binding.formUserName.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_500))
                binding.formUserName.background.setTint(backgroundColor)

                binding.formUserLastname.isEnabled = false
                binding.formUserLastname.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_500))
                binding.formUserLastname.background.setTint(backgroundColor)

                binding.formUserDocument.isEnabled = false
                binding.formUserDocument.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_500))
                binding.formUserDocument.background.setTint(backgroundColor)
            }
        }
    }

    private fun showBottomSheetDialogBank() {
        val bottomSheet = HaveAccountBottomSheetDialog { selectedOption ->
            Log.d("PersonalInfoFormFragment", "Opción: ${selectedOption}")
            binding.formUserHaveAccount.setText(selectedOption)
        }
        bottomSheet.show(parentFragmentManager, "HaveAccountBottomSheetDialog")
    }

    private fun showBottomSheetDialogUser() {
        val bottomSheet = UserEmployedBottomSheetDialog { selectedOption ->
            Log.d("PersonalInfoFormFragment", "Opción: ${selectedOption}")
            binding.formUserEmploye.setText(selectedOption)
        }
        bottomSheet.show(parentFragmentManager, "UserEmployedBottomSheetDialog")
    }

    /*CUANDO el usuario ya existe*/
    private fun showHaveAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_have_account, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogView.findViewById<TextView>(R.id.btn_close_dialog).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.btn_submit_login).setOnClickListener {
            navigateToLoginFragment()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun navigateToLoginFragment() {
        val navController = findNavController()
        navController.navigate(R.id.loginFragment)
    }


    private fun setupListeners() {

        binding.editTextDate.setOnClickListener {
            val datePickerDialog = DatePickerBottomSheetDialog { selectedDate ->
                binding.editTextDate.setText(selectedDate) // Muestra la fecha completa en el EditText
            }
            datePickerDialog.show(parentFragmentManager, "datePicker")
        }

        binding.btnNextStep.setOnClickListener {

            if (validateForm()) {

                // Habilitar enví ode datos
                canContinue = true
                Log.d("Personal", "Habilitando botón")

                val sessionId = helper.lexisNexis()
                //println("Session ID: $sessionId")

                val firstNameEditText = binding.root.findViewById<EditText>(R.id.form_user_name)
                val lastNameEditText = binding.root.findViewById<EditText>(R.id.form_user_lastname)
                val documentEditText = binding.root.findViewById<EditText>(R.id.form_user_document)
                val dateEditText = binding.root.findViewById<EditText>(R.id.editTextDate)
                val phoneEditText = binding.root.findViewById<EditText>(R.id.form_user_phone)
                val passwordEditText = binding.root.findViewById<EditText>(R.id.form_password_edit_text)
                val emailEditText = binding.root.findViewById<EditText>(R.id.form_user_email)
                val validateEmailEditText = binding.root.findViewById<EditText>(R.id.form_user_validate_email)

                // Formato de fechas
                val inputDate = dateEditText.text.toString()
                val formattedDate = formatDateToYYYYMMDD(inputDate)

                // Enviar datos
                request = LoanStepOneRequest(
                    contacto = userContactId,
                    nombre = firstNameEditText.text.toString().trim(),
                    primerApellido = lastNameEditText.text.toString().trim(),
                    numeroDocumento = documentEditText.text.toString().trim(),
                    fechaNacimiento = formattedDate,
                    celular = phoneEditText.text.toString().trim(),
                    empleadoFormal = binding.formUserEmploye.text.toString().trim(),
                    nombreCuentaBancaria = binding.formUserHaveAccount.text.toString().trim(),
                    correoElectronico = emailEditText.text.toString().trim(),
                    //referido_cliente_cedula = null,
                    //referido_cliente = null,
                    //referido_cliente_id = null,
                    tipoDocumento = "Cedula de Ciudadania",
                    plazoSeleccionado = loanTerm,
                    valorSeleccionado = loanAmount,
                    //ingresoMensual = 1000000,
                    fechaExpedicion = "2001-06-15",
                    sessionId = sessionId,
                    genero = "Masculino"
                )

                // campos para usuarios nuevos
                if(!registeredUser){
                    request.password = passwordEditText.text.toString().trim()
                    request.correoElectronico2 = validateEmailEditText.text.toString().trim()
                }

                Log.d("PersonalInfoFormFragment", "Params: ${request}")

                // Loading
                //val loadingDialog = LoadingDialogFragment()
                //loadingDialog.show(parentFragmentManager, "LoadingDialog") // Mostrar loading

                loadingDialog.show(parentFragmentManager, "LoadingDialog")

                lifecycleScope.launch {
                    try {

                        // Configurar respusta para obtener datos en fragment BankInfoFormFragment
                        /*val listenerResponse = withContext(Dispatchers.IO) {
                            loanViewModel.getData(request)
                        }*/

                        val loanStepOneResponse = withContext(Dispatchers.IO) {
                            loanRepository.getData(request) // Llamada suspend
                            ////loanViewModel.getData(request)
                        }
                        if(loanStepOneResponse != null){
                            loanViewModel.setData(loanStepOneResponse)
                        }

                        //dismissLoadingDialog()
                        loadingDialog.dismiss()

                        // observer
                        if(loanStepOneResponse?.solicitud?.codigo?.isNotEmpty() == true){
                            processData(loanStepOneResponse.solicitud)
                        }

                    } catch (e: Exception) {
                        loadingDialog.dismiss() // Ocultar en caso de error
                        Log.e("PersonalInfoFormFragment", "Error obteniendo datos", e)
                        // Mostrar errores
                    }
                }

            } else {
                Log.e("PersonalInfoFormFragment", "Corrige los errores antes de continuar")
                //Toast.makeText(requireContext(), "Corrige los errores antes de continuar", Toast.LENGTH_SHORT).show()
                // Test
                /*val loadingDialog = LoadingDialogFragment()
                loadingDialog.show(parentFragmentManager, "LoadingDialog")
                lifecycleScope.launch {
                    delay(2000) // Simular un tiempo de espera
                    loadingDialog.dismiss()
                }
                // Siguiente pantalla
                (parentFragment as? FormFragment)?.goToNextStep()*/
            }
        }

        val checkBoxAgree = binding.root.findViewById<CheckBox>(R.id.checkbox_user_agree)
        val checkBoxTerms = binding.root.findViewById<CheckBox>(R.id.checkbox_user_terms)

        val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
        val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

        checkBoxAgree.setOnCheckedChangeListener { _, isChecked ->
            checkBoxAgree.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
        }
        checkBoxTerms.setOnCheckedChangeListener { _, isChecked ->
            checkBoxTerms.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
        }

    }

    // Mostrar errores de usuarios previamente registrados, reemplazar luego por modal
    private fun showAccountErrors(errorData: Solicitud) {
        // Gestión de validaciones
        val documentEditText = binding.root.findViewById<EditText>(R.id.form_user_document)
        val phoneEditText = binding.root.findViewById<EditText>(R.id.form_user_phone)
        val emailEditText = binding.root.findViewById<EditText>(R.id.form_user_email)
        // Mensajes de error para los campos
        val errorDocument = binding.errorUserDocument
        val errorPhone = binding.errorPhone
        val errorEmail = binding.errorUserEmail

        // Ocultar todos los mensajes de error al inicio
        errorDocument.visibility = View.GONE
        errorPhone.visibility = View.GONE
        errorEmail.visibility = View.GONE

        if(errorData.codigo == duplicateIdDocumentCode && errorData.result == duplicateIdDocumentMessage){
            errorDocument.text = idDocumentErrorMessage
            errorDocument.visibility = View.VISIBLE
            documentEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            // Propiedad fpcus para el campo
            documentEditText.requestFocus()
        }
        else if(errorData.codigo == duplicatePhoneNumberCode && errorData.result == duplicatePhoneNumberMessage){
            errorPhone.text = phoneNumberErrorMessage
            errorPhone.visibility = View.VISIBLE
            phoneEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            phoneEditText.requestFocus()
        }
        else if(errorData.codigo == duplicateEmailCode && errorData.result == duplicateEmailMessage){
            errorEmail.text = emailErrorMessage
            errorEmail.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            emailEditText.requestFocus()
        }

    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Gestión de validaciones
        val firstNameEditText = binding.root.findViewById<EditText>(R.id.form_user_name)
        val lastNameEditText = binding.root.findViewById<EditText>(R.id.form_user_lastname)
        val documentEditText = binding.root.findViewById<EditText>(R.id.form_user_document)
        val dateEditText = binding.root.findViewById<EditText>(R.id.editTextDate)
        val phoneEditText = binding.root.findViewById<EditText>(R.id.form_user_phone)
        val emailEditText = binding.root.findViewById<EditText>(R.id.form_user_email)
        val validateEmailEditText = binding.root.findViewById<EditText>(R.id.form_user_validate_email)
        val employeEditText = binding.root.findViewById<EditText>(R.id.form_user_employe)
        val haveAccountEditText = binding.root.findViewById<EditText>(R.id.form_user_have_account)
        val checkBoxAgree = binding.root.findViewById<CheckBox>(R.id.checkbox_user_agree)
        val checkBoxTerms = binding.root.findViewById<CheckBox>(R.id.checkbox_user_terms)
        val passwordEditText = binding.root.findViewById<EditText>(R.id.form_password_edit_text)
        // Mensajes de error para los campos
        val errorFirstName = binding.errorUserName
        val errorLastName = binding.errorUserLastname
        val errorDocument = binding.errorUserDocument
        val errorDate = binding.errorDate
        val errorPhone = binding.errorPhone
        val errorEmail = binding.errorUserEmail
        val errorValidateEmail = binding.errorValidateEmail
        val errorEmploye = binding.errorUserEmploye
        val errorHaveAccount = binding.errorUserHaveAccount
        val errorPassword = binding.errorUserPassword

        // Ocultar todos los mensajes de error al inicio
        errorFirstName.visibility = View.GONE
        errorLastName.visibility = View.GONE
        errorDocument.visibility = View.GONE
        errorDate.visibility = View.GONE
        errorPhone.visibility = View.GONE
        errorEmail.visibility = View.GONE
        errorValidateEmail.visibility = View.GONE
        errorEmploye.visibility = View.GONE
        errorHaveAccount.visibility = View.GONE
        errorPassword.visibility = View.GONE

        // Validar primer nombre
        if (firstNameEditText.text.toString().trim().isEmpty()) {
            errorFirstName.text = "Por favor, ingresa tu nombre"
            errorFirstName.visibility = View.VISIBLE
            firstNameEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            firstNameEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar apellido
        if (lastNameEditText.text.toString().trim().isEmpty()) {
            errorLastName.text = "Por favor, ingresa tu apellido"
            errorLastName.visibility = View.VISIBLE
            lastNameEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            lastNameEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar cédula
        if (documentEditText.text.toString().trim().isEmpty()) {
            errorDocument.text = "Por favor, ingresa tu número de cédula"
            errorDocument.visibility = View.VISIBLE
            documentEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            documentEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar fecha de nacimiento
        if (dateEditText.text.toString().trim().isEmpty()) {
            errorDate.text = "Por favor, seleccione tu fecha de nacimiento"
            errorDate.visibility = View.VISIBLE
            dateEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            dateEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar teléfono
        if (phoneEditText.text.toString().trim().isEmpty()) {
            errorPhone.text = "Por favor, ingresa tu teléfono"
            errorPhone.visibility = View.VISIBLE
            phoneEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            phoneEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar correo electrónico
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            errorEmail.text = "Por favor, ingresa tu correo electrónico"
            errorEmail.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.text = "Correo electrónico inválido"
            errorEmail.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            emailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar si es Empleado formal
        val formalEmployeeSelectedOption = binding.formUserEmploye.text.toString()
        if (formalEmployeeSelectedOption.trim().isEmpty() || formalEmployeeSelectedOption.trim() == DEFAULT_SPINNER_SELECTED_OPTION) {
            errorEmploye.text = "Por favor, selecciona una respuesta"
            errorEmploye.visibility = View.VISIBLE
            binding.formUserEmploye.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            binding.formUserEmploye.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar si tiene cuenta bancaria
        val haveAccountSelectedOption = binding.formUserHaveAccount.text.toString()
        if (haveAccountSelectedOption.trim().isEmpty() || haveAccountSelectedOption.trim() == DEFAULT_SPINNER_SELECTED_OPTION) {
            errorHaveAccount.text = "Por favor, selecciona una respuesta"
            errorHaveAccount.visibility = View.VISIBLE
            binding.formUserHaveAccount.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            binding.formUserHaveAccount.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar CheckBox de términos y condiciones
        if (!checkBoxAgree.isChecked) {
            checkBoxAgree.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.error_color))
            isValid = false
        } else {
            checkBoxAgree.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.woodsmoke_600))
        }

        if (!checkBoxTerms.isChecked) {
            checkBoxTerms.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.error_color))
            isValid = false
        } else {
            checkBoxTerms.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.woodsmoke_600))
        }

        // Excluir campos para usuario registrados
        if(!registeredUser){
            // Validar confirmación de correo electrónico
            val validateEmail = validateEmailEditText.text.toString().trim()
            if (validateEmail.isEmpty()) {
                errorValidateEmail.text = "Por favor, valida tu correo electrónico"
                errorValidateEmail.visibility = View.VISIBLE
                validateEmailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                isValid = false
            } else if (validateEmail != email) {
                errorValidateEmail.text = "Los correos electrónicos no coinciden"
                errorValidateEmail.visibility = View.VISIBLE
                validateEmailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                isValid = false
            } else {
                validateEmailEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
            }

            // Validar password
            if (passwordEditText.text.toString().trim().isEmpty()) {
                errorPassword.text = "Por favor, ingresa una contraseña"
                errorPassword.visibility = View.VISIBLE
                passwordEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                isValid = false
            } else {
                passwordEditText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
            }
        }

        return isValid

    }

    fun formatDateToYYYYMMDD(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato esperado de entrada
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato de salida deseado
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date()) // Si es null, usa la fecha actual por defecto
        } catch (e: Exception) {
            "" // Si falla, devuelve este valor por defecto
        }
    }

    private fun showDataTreatmentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_data_treatment, null)

        val dialog = Dialog(requireContext(), R.style.FullScreenDialog).apply {
            setContentView(dialogView)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }

        dialogView.findViewById<ImageView>(R.id.back_toolbar_icon).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showTermsAndConditionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_conditions, null)

        val dialog = Dialog(requireContext(), R.style.FullScreenDialog).apply {
            setContentView(dialogView)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }

        dialogView.findViewById<ImageView>(R.id.back_toolbar_icon_terms).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}