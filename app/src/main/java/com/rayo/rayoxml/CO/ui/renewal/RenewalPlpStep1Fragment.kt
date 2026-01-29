package com.rayo.rayoxml.co.ui.renewal

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.GenericAdapter
import com.rayo.rayoxml.databinding.FragmentPlpStep1Binding
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.PLPLoanStep1Request
import com.rayo.rayoxml.co.services.Loan.PLPLoanStep2Request
import com.rayo.rayoxml.co.services.Loan.PlpLoanStep1Response
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.co.viewModels.RenewalViewModel
import com.rayo.rayoxml.utils.TermsAndConditionsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

class RenewalPlpStep1Fragment : Fragment() {

    private var _binding: FragmentPlpStep1Binding? = null
    private val binding get() = _binding!!
    private lateinit var editTextAntiguedadLaboral: EditText

    private lateinit var repository: LoanRepository
    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    private val renewalViewModel: RenewalViewModel by activityViewModels()
    private var formId: String? = null

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel
    private var userHasLoans: Boolean = false
    private var userHasActiveLoans: Boolean = true
    private var userContactId: String = ""

    // Declaración de variables
    private lateinit var name: EditText
    private lateinit var errorName: TextView
    private lateinit var lastName: EditText
    private lateinit var errorLastName: TextView
    private lateinit var document: EditText
    private lateinit var errorDocument: TextView
    private lateinit var phone: EditText
    private lateinit var errorPhone: TextView
    private lateinit var email: EditText
    private lateinit var errorEmail: TextView
    private lateinit var ingresosMensuales: EditText
    private lateinit var errorIngresosMensuales: TextView
    private lateinit var antiguedadLaboral: EditText
    private lateinit var errorAntiguedadLaboral: TextView
    private lateinit var cuantosHijos: EditText
    private lateinit var errorCuantosHijos: TextView
    private lateinit var personasDependen: EditText
    private lateinit var errorPersonasDependen: TextView
    private lateinit var contraseñaMiembros: EditText
    private lateinit var errorContraseñaMiembros: TextView
    private lateinit var checkboxTerms: CheckBox
    private lateinit var verifyInformation: TextView

    private lateinit var textViewTermsAndConditions: TextView

    // TyC
    private lateinit var dialogView: View
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_plp_step1, container, false)

        val btnNextStep = view.findViewById<Button>(R.id.btnNextStep)
        btnNextStep.setOnClickListener {
            Log.d("RenewalPlpStep1Fragment", "Paso 1")
            //(requireParentFragment() as? RenewalFragment)?.goToNextStep()
            if (validateForm()) {
                // Actualizar datos viewmodel
                val formData = PLPLoanStep2Request(
                    antiguedadLaboral = antiguedadLaboral.text.toString(),
                    cantidadDependientes = personasDependen.text.toString().toInt(),
                    cantidadHijos = cuantosHijos.text.toString().toInt(),
                    celular = phone.text.toString(),
                    contacto = "", // se toma en pantalla paso 3
                    correoElectronico = email.text.toString(),
                    fechaExpedicion = "2021-01-01",
                    ingresoMensual = ingresosMensuales.text.toString(),
                    nombre = name.text.toString(),
                    numeroDocumento = document.text.toString(),
                    password = contraseñaMiembros.text.toString(),
                    primerApellido = lastName.text.toString(),
                    tipoDocumento = "Cedula de Ciudadania"
                )
                renewalViewModel.setPlpStep2Request(formData)
                // Proceder al siguiente paso
                lifecycleScope.launch {
                    step1()
                }
            }else{
                Log.d("RenewalPlpStep1Fragment", "Errores formulario Paso 1")
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TyC
        dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_conditions, null)
        dialog = Dialog(requireContext(), R.style.FullScreenDialog).apply {
            setContentView(dialogView)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }

        // Inicialización de variables con findViewById
        name = view.findViewById(R.id.form_user_name)
        errorName = view.findViewById(R.id.error_user_name)
        lastName = view.findViewById(R.id.form_user_lastname)
        errorLastName = view.findViewById(R.id.error_user_lastname)
        document = view.findViewById(R.id.form_user_document)
        errorDocument = view.findViewById(R.id.error_user_document)
        phone = view.findViewById(R.id.form_user_phone)
        errorPhone = view.findViewById(R.id.error_phone)
        email = view.findViewById(R.id.form_user_email)
        errorEmail = view.findViewById(R.id.error_user_email)
        ingresosMensuales = view.findViewById(R.id.form_ingresos_mensuales)
        errorIngresosMensuales = view.findViewById(R.id.error_ingresos_mensuales)
        antiguedadLaboral = view.findViewById(R.id.form_antiguedad_laboral)
        errorAntiguedadLaboral = view.findViewById(R.id.error_antiguedad_laboral)
        cuantosHijos = view.findViewById(R.id.form_cuantos_hijos)
        errorCuantosHijos = view.findViewById(R.id.error_cuantos_hijos)
        personasDependen = view.findViewById(R.id.form_personas_dependen)
        errorPersonasDependen = view.findViewById(R.id.error_personas_dependen)
        contraseñaMiembros = view.findViewById(R.id.form_contraseña_miembros_editText)
        errorContraseñaMiembros = view.findViewById(R.id.error_contraseña_miembros)
        checkboxTerms = view.findViewById(R.id.checkbox_user_terms)
        verifyInformation = view.findViewById(R.id.textViewVerifyInformation)

        textViewTermsAndConditions = view.findViewById(R.id.textViewTermsAndConditions)

        repository = LoanRepository()
        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        // Obtener ViewModel compartido con la actividad
        val repository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(repository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]

        editTextAntiguedadLaboral = view.findViewById(R.id.form_antiguedad_laboral)
        val viaList = listOf("3 a 6 meses", "6 a 12 meses", "1 a 2 años", "más de 2 años")
        // Asignar opción por defecto
        editTextAntiguedadLaboral.setText(viaList.first())
        editTextAntiguedadLaboral.setOnClickListener {
            showBottomSheetDialog(viaList, editTextAntiguedadLaboral)
        }

        // Observar los datos del usuario
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                userContactId = user.contacto!!
                Log.d("RenewalPlpStep1Fragment", "Contact ID: $userContactId")
                lifecycleScope.launch {
                    loadData()
                }
            }
        }
        // Fin Observar los datos del usuario

        // Check términos
        checkboxTerms.setOnClickListener {
            // Validar que los términos y condiciones estén aceptados
            val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
            val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

            val isChecked = checkboxTerms.isChecked
            checkboxTerms.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
        }

        // Asignar OnClickListener al TextView de "términos y condiciones"
        textViewTermsAndConditions.setOnClickListener {
            showTermsAndConditionsDialog()
        }
    }
    
    private suspend fun loadData(){
        val formData = PLPLoanStep1Request(
            contacto = userContactId
        )
        Log.d("RenewalPlpStep1Fragment", "STEP 1 form: ${formData}")

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val plpLoanStep1Response = withContext(Dispatchers.IO) {
                repository.getDataPlpStep1(formData)
            }

            Log.d("RenewalPlpStep1Fragment", "PLP loan step 1 Data: ${plpLoanStep1Response}")

            if(plpLoanStep1Response != null && plpLoanStep1Response.solicitud.codigo == "200"){
                val data = plpLoanStep1Response.solicitud

                // Obtener datos de tyc
                loadTermsAndConditionsData(plpLoanStep1Response)

                // cargar datos
                if(data != null){
                    // Asignar datos para términos
                    renewalViewModel.setPlpStep1Response(data)

                    name.setText(data.nombre)
                    lastName.setText(data.apellido)
                    document.setText(data.numeroDocumento)
                    phone.setText(data.celular)
                    email.setText(data.correo)
                    ingresosMensuales.setText(ceil(data.ingresos.toDouble()).toInt().toString())
                    antiguedadLaboral.setText(data.antiguedad)
                    cuantosHijos.setText(data.hijos)
                    personasDependen.setText(data.dependientes)

                    // deshabilitar campos principales
                    val backgroundColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_300)

                    name.isEnabled = false
                    name.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_500))
                    name.background.setTint(backgroundColor)

                    lastName.isEnabled = false
                    lastName.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_500))
                    lastName.background.setTint(backgroundColor)

                    document.isEnabled = false
                    document.setTextColor(ContextCompat.getColor(requireContext(), R.color.woodsmoke_500))
                    document.background.setTint(backgroundColor)
                }

            }else{
                Log.d("RenewalPlpStep1Fragment", "Error PLP loan step 1 Data")
            }

            loadingDialog.dismiss() // ocultar loading
        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("RenewalPlpStep1Fragment", "Error obteniendo datos", e)
        }

    }

    private suspend fun step1(){
        (requireParentFragment() as? RenewalFragment)?.goToNextStep()
        /*val formData = PLPLoanStep1Request(
            contacto = userContactId
        )
        Log.d("RenewalPlpStep1Fragment", "STEP 1 form: ${formData}")

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val plpLoanStep1Response = withContext(Dispatchers.IO) {
                repository.getDataPlpStep1(formData)
            }

            Log.d("RenewalPlpStep1Fragment", "PLP loan step 1 Data: ${plpLoanStep1Response}")

            if(plpLoanStep1Response != null && plpLoanStep1Response.solicitud.codigo == "200"){

                (requireParentFragment() as? RenewalFragment)?.goToNextStep()

            }else{
                Log.d("RenewalPlpStep1Fragment", "Error PLP loan step 1 Data")
            }

            loadingDialog.dismiss() // ocultar loading
        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("RenewalPlpStep1Fragment", "Error obteniendo datos", e)
        }*/
    }

    private fun loadTermsAndConditionsData(plpLoanStep1Response: PlpLoanStep1Response) {

        val termsContainer = dialogView.findViewById<LinearLayout>(R.id.terms_container)
        TermsAndConditionsHelper(requireContext(), termsContainer, dialogView).setupTermsAndConditions(plpLoanStep1Response.solicitud)

        dialogView.findViewById<ImageView>(R.id.back_toolbar_icon_terms).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showTermsAndConditionsDialog() {
        /*val dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_conditions, null)
        val dialog = Dialog(requireContext(), R.style.FullScreenDialog).apply {
            setContentView(dialogView)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }*/

        /*val termsContainer = dialogView.findViewById<LinearLayout>(R.id.terms_container)
        TermsAndConditionsHelper(requireContext(), termsContainer).setupTermsAndConditions()

        dialogView.findViewById<ImageView>(R.id.back_toolbar_icon_terms).setOnClickListener {
            dialog.dismiss()
        }*/
        dialog.show()
    }

    private fun showBottomSheetDialog(list: List<String>, editText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_antiguedad, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewAntiguedad)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = GenericAdapter(list) { selectedItem ->
            editText.setText(selectedItem)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        fun validateField(editText: EditText, errorTextView: TextView, errorMessage: String, minLength: Int? = null): Boolean {
            val value = editText.text.toString().trim()
            return if (value.isEmpty()) {
                errorTextView.text = errorMessage
                errorTextView.visibility = View.VISIBLE
                editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                editText.requestFocus()
                false
            } else if (minLength != null && value.length < minLength) {
                errorTextView.text = "Debe tener al menos $minLength caracteres"
                errorTextView.visibility = View.VISIBLE
                editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
                editText.requestFocus()
                false
            } else {
                errorTextView.visibility = View.GONE
                editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
                true
            }
        }

        isValid = validateField(name, errorName, "Por favor, ingrese su primer nombre") && isValid
        isValid = validateField(lastName, errorLastName, "Por favor, ingrese su apellido") && isValid
        isValid = validateField(document, errorDocument, "Por favor, ingrese su número de documento", 8) && isValid
        isValid = validateField(phone, errorPhone, "Por favor, ingrese su teléfono", 7) && isValid
        isValid = validateField(email, errorEmail, "Por favor, ingrese su correo electrónico") && isValid
        isValid = validateField(ingresosMensuales, errorIngresosMensuales, "Por favor, ingrese un valor") && isValid

        // Validar ingresos mayores a 0
        val ingresos = ingresosMensuales.text.toString().toIntOrNull() ?: 0
        if (ingresos <= 0) {
            errorIngresosMensuales.text = "Debe ser mayor a 0"
            errorIngresosMensuales.visibility = View.VISIBLE
            ingresosMensuales.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            ingresosMensuales.requestFocus()
            isValid = false
        } else {
            errorIngresosMensuales.visibility = View.GONE
            ingresosMensuales.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        isValid = validateField(antiguedadLaboral, errorAntiguedadLaboral, "Por favor, seleccione una opción") && isValid
        isValid = validateField(cuantosHijos, errorCuantosHijos, "Por favor, ingrese cuántos hijos tiene") && isValid
        isValid = validateField(personasDependen, errorPersonasDependen, "Por favor, ingrese un valor") && isValid
        isValid = validateField(contraseñaMiembros, errorContraseñaMiembros, "Por favor, ingrese una contraseña") && isValid

        // Validar que los términos y condiciones estén aceptados

        // Validar CheckBox
        if (!checkboxTerms.isChecked) {
            //checkboxTerms.text = "Debe aceptar los términos y condiciones."
            checkboxTerms.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.error_color))
            isValid = false
        } else {
            //checkboxTerms.text = null
            //checkboxTerms.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.woodsmoke_600))
        }

        // Mensaje validar información
        verifyInformation.visibility = if (isValid) View.GONE else View.VISIBLE

        return isValid
    }

}
