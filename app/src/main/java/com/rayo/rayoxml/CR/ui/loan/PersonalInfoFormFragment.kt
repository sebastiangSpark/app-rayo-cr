package com.rayo.rayoxml.cr.ui.loan

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.ui.dialogs.DatePickerBottomSheetDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.rayo.rayoxml.cr.adapters.CivilStateAdapter
import com.rayo.rayoxml.cr.adapters.GenderAdapter
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.viewModels.FormViewModel
import com.rayo.rayoxml.databinding.CrPersonalInfoFormBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PersonalInfoFormFragment(private val userParamsViewModel: UserViewModel) : Fragment() {

    private var _binding: CrPersonalInfoFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var editTextGender: EditText
    private lateinit var editTextCivilState: EditText

    private val formViewModel: FormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CrPersonalInfoFormBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNextStep.setOnClickListener {

            if (validateForm()) {

                // Habilitar envío de datos
                //canContinue = true
                //Log.d("Personal", "Habilitando botón")

                // Actualizar datos
                formViewModel.updatePersonalInfo(
                    nombreCompleto = binding.formUserName.text.toString().trim(),
                    apellidos = binding.formUserLastname.text.toString().trim(),
                    numeroCedula = binding.formUserDocument.text.toString().trim(),
                    fechaExpiracionCedula = formatDateToYYYYMMDD(
                        binding.editTextDateDoc.text.toString().trim()
                    ),
                    fechaNacimiento = formatDateToYYYYMMDD(
                        binding.editTextDate.text.toString().trim()
                    ),
                    genero = binding.formGender.text.toString().trim(),
                    estadoCivil = binding.formCivilState.text.toString().trim(),
                    celular = binding.formUserPhone.text.toString().trim(),
                    correo = binding.formUserEmail.text.toString().trim(),
                    clave = binding.formPasswordEditText.text.toString().trim(),
                )

                // Continuar
                (parentFragment as? FormFragment)?.goToNextStep()

                // Obtener token
                /*lifecycleScope.launch {
                    val authResponse = authRepository.getToken()
                    if (authResponse != null && authResponse.accessToken.isNotEmpty()){
                        Log.d("PersonalInfoFormFragment", "Auth Data: ${authResponse}")
                    }else{
                        // Pantalla de rechazo
                    }
                }*/
            } else {
                // Corregir datos
                //(parentFragment as? FormFragment)?.goToNextStep()
            }
        }

        binding.editTextDate.setOnClickListener {
            val datePickerDialog = DatePickerBottomSheetDialog { selectedDate ->
                binding.editTextDate.setText(selectedDate) // Muestra la fecha completa en el EditText
            }
            datePickerDialog.show(parentFragmentManager, "datePicker")
        }

        binding.editTextDateDoc.setOnClickListener {
            val datePickerDialog = DatePickerBottomSheetDialog { selectedDate ->
                binding.editTextDateDoc.setText(selectedDate) // Muestra la fecha completa en el EditText
            }
            datePickerDialog.show(parentFragmentManager, "datePicker")
        }

        editTextGender = view.findViewById(R.id.form_gender)
        editTextCivilState = view.findViewById(R.id.form_civil_state)

        editTextGender.setOnClickListener {
            showBottomSheetDialogGender()
        }

        editTextCivilState.setOnClickListener {
            showBottomSheetDialogCivilState()
        }

        binding.editTextDate.setSingleClickListener {
            showMaterialDatePickerBottomSheet()
        }
        binding.editTextDateDoc.setSingleClickListener {
            showMaterialDatePickerDocBottomSheet()
        }
    }


    fun View.setSingleClickListener(delay: Long = 600, action: () -> Unit) {
        var lastClickTime = 0L
        setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < delay) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            action()
        }
    }

    private fun showMaterialDatePickerDocBottomSheet() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Fecha de expiración de la cédula*")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTheme(R.style.Theme_MyApp_DatePicker) // 👈 Aplica tu tema personalizado
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val formattedDate = sdf.format(Date(selectedDateInMillis))
            binding.editTextDateDoc.setText(formattedDate)
        }

        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun showMaterialDatePickerBottomSheet() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Fecha de nacimiento*")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTheme(R.style.Theme_MyApp_DatePicker) // 👈 Aplica tu tema personalizado
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val formattedDate = sdf.format(Date(selectedDateInMillis))
            binding.editTextDate.setText(formattedDate)
        }

        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun showBottomSheetDialogGender() {
        editTextGender.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_gender, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextGender.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewGender)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = GenderAdapter { selectedType ->
            binding.formGender.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextGender.isEnabled = true
        }
    }

    private fun showBottomSheetDialogCivilState() {
        editTextCivilState.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_civil_state, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextCivilState.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCivilState)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CivilStateAdapter { selectedType ->
            binding.formCivilState.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextCivilState.isEnabled = true
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validaciones
        val firstNameEditText = binding.formUserName
        val lastNameEditText = binding.formUserLastname
        val documentEditText = binding.formUserDocument
        val expireDateEditText = binding.editTextDateDoc
        val birthDateEditText = binding.editTextDate
        val gender = binding.formGender
        val maritalStatus = binding.formCivilState
        val phoneEditText = binding.formUserPhone
        val emailEditText = binding.formUserEmail

        // Mensajes de error para los campos
        val errorFirstName = binding.errorUserName
        val errorLastName = binding.errorUserLastname
        val errorDocument = binding.errorUserDocument
        val errorDate = binding.errorDate
        val errorExpireDate = binding.errorExpireDate
        val errorPhone = binding.errorPhone
        val errorEmail = binding.errorUserEmail
        val errorPassword = binding.errorUserPassword
        val errorGender = binding.errorGender
        val errorMaritalStatus = binding.errorMaritalStatus

        // Ocultar todos los mensajes de error al inicio
        errorFirstName.visibility = View.GONE
        errorLastName.visibility = View.GONE
        errorDocument.visibility = View.GONE
        errorDate.visibility = View.GONE
        errorPhone.visibility = View.GONE
        errorEmail.visibility = View.GONE
        errorExpireDate.visibility = View.GONE
        errorGender.visibility = View.GONE
        errorMaritalStatus.visibility = View.GONE
        errorPassword.visibility = View.GONE

        // Validar nombre
        if (firstNameEditText.text.toString().trim().isEmpty()) {
            errorFirstName.text = "Por favor, ingrese su nombre"
            errorFirstName.visibility = View.VISIBLE
            firstNameEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            firstNameEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar apellidos
        if (lastNameEditText.text.toString().trim().isEmpty()) {
            errorLastName.text = "Por favor, ingrese sus apellidos"
            errorLastName.visibility = View.VISIBLE
            lastNameEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            lastNameEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar cédula
        if (documentEditText.text.toString().trim().isEmpty()) {
            errorDocument.text = "Por favor, ingrese su número de cédula"
            errorDocument.visibility = View.VISIBLE
            documentEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            documentEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar fechas
        if (expireDateEditText.text.toString().trim().isEmpty()) {
            errorExpireDate.text = "Por favor, seleccione la fecha de expiración de su cédula"
            errorExpireDate.visibility = View.VISIBLE
            expireDateEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            expireDateEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }
        // Validate birth date
        var errorMessageBirthdate = "";
        val minimumAge = 18
        if (birthDateEditText.text.toString().trim().isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val birthDate = dateFormat.parse(birthDateEditText.text.toString().trim())
                val currentDate = Date()
                if ((currentDate.year - birthDate?.year!!) < 18 ||
                    ((currentDate.year - birthDate.year) == 18 && (currentDate.month - birthDate.month) < 0) ||
                    ((currentDate.year - birthDate.year) == 18 && (currentDate.month - birthDate.month) == 0 && (currentDate.date - birthDate.date) < 0)
                ) {
                    errorMessageBirthdate =
                        "${getString(R.string.error_minimum_age)} $minimumAge ${getString(R.string.years)}"
                }
            } catch (e: Exception) {
                errorMessageBirthdate = getString(R.string.error_invalid_date)
            }
        } else {
            errorMessageBirthdate = getString(R.string.error_empty_date)
        }

        if (errorMessageBirthdate.isEmpty()) {
            birthDateEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        } else {
            errorDate.text = errorMessageBirthdate
            errorDate.visibility = View.VISIBLE
            birthDateEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        }

        // Validar género
        val genderSelectedOption = gender.text.toString()
        if (genderSelectedOption.trim().isEmpty()) {
            errorGender.text = "Por favor, seleccione un género"
            errorGender.visibility = View.VISIBLE
            gender.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            gender.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar estado marital
        val maritalStatusSelectedOption = maritalStatus.text.toString()
        if (maritalStatusSelectedOption.trim().isEmpty()) {
            errorMaritalStatus.text = "Por favor, seleccione el estado civil"
            errorMaritalStatus.visibility = View.VISIBLE
            maritalStatus.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            maritalStatus.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar teléfono
        var errorMessagePhone = "";
        if (phoneEditText.text.toString().isNotEmpty()) {
            val phoneNumber = phoneEditText.text.toString().trim()
            if (phoneNumber.length != 8) {
                errorMessagePhone = getString(R.string.error_invalid_phone)
            }
        } else {
            errorMessagePhone = getString(R.string.error_empty_phone)
        }

        if (errorMessagePhone.isEmpty()) {
            phoneEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        } else {
            errorPhone.text = errorMessagePhone
            errorPhone.visibility = View.VISIBLE
            phoneEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        }

        // Validar correo electrónico
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            errorEmail.text = "Por favor, ingrese un correo electrónico"
            errorEmail.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.text = "Correo electrónico inválido"
            errorEmail.visibility = View.VISIBLE
            emailEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false
        } else {
            emailEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Password validations
        var passwordFailingValidations = "";
        if (binding.formPasswordEditText.text.toString().length < 8) {
            passwordFailingValidations += "- ${getString(R.string.error_minimum_characters)}\n"
        }
        if (!binding.formPasswordEditText.text.toString().matches(Regex(".*\\d.*"))) {
            passwordFailingValidations += "- ${getString(R.string.error_minimum_one_number)}\n"
        }
        if (!binding.formPasswordEditText.text.toString().matches(Regex(".*[A-Z].*"))) {
            passwordFailingValidations += "- ${getString(R.string.error_minimum_one_uppercase)}\n"
        }
        if (!binding.formPasswordEditText.text.toString().matches(Regex(".*[a-z].*"))) {
            passwordFailingValidations += "- ${getString(R.string.error_minimum_one_lowercase)}\n"
        }
        if (!binding.formPasswordEditText.text.toString()
                .matches(Regex(".*[!@#\$%^&*()_\\-+=\\[\\]{}:;,.?].*"))
        ) {
            passwordFailingValidations += "- ${getString(R.string.error_minimum_one_symbol)}"
        }
        if (passwordFailingValidations.isEmpty()) {
            binding.formPasswordEditText.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        } else {
            errorPassword.text = passwordFailingValidations
            errorPassword.visibility = View.VISIBLE
            binding.formPasswordEditText.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.custom_input_background_error
            )
            isValid = false

        }
        return isValid
    }

    fun formatDateToYYYYMMDD(dateString: String): String {
        return try {
            val inputFormat =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato esperado de entrada
            val outputFormat =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Formato de salida deseado
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date()) // Si es null, usa la fecha actual por defecto
        } catch (e: Exception) {
            "" // Si falla, devuelve este valor por defecto
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}