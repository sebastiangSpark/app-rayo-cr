package com.rayo.rayoxml.co.ui.renewal

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.BankAdapter
import com.rayo.rayoxml.co.adapters.GenericAdapter
import com.rayo.rayoxml.co.adapters.TypeAccountAdapter
import com.rayo.rayoxml.databinding.FragmentPlpStep3Binding
import com.rayo.rayoxml.co.models.loadBanksFromJson
import com.rayo.rayoxml.co.services.Loan.LoanRepository
import com.rayo.rayoxml.co.services.Loan.PLPLoanStep2Request
import com.rayo.rayoxml.co.services.Loan.PLPLoanStep3Request
import com.rayo.rayoxml.co.services.User.UserRepository
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.co.services.User.UserViewModelFactory
import com.rayo.rayoxml.co.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.utils.PreferencesManager
import com.rayo.rayoxml.co.viewModels.RenewalViewModel
import com.rayo.rayoxml.co.viewModels.FormViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RenewalPlpStep3Fragment : Fragment() {

    private var _binding: FragmentPlpStep3Binding? = null
    private val binding get() = _binding!!

    //private lateinit var editTextContractType: EditText
    //private lateinit var editTextServiceStep3: EditText
    private val banks  by lazy { loadBanksFromJson(requireContext()) }
    private lateinit var repository: LoanRepository
    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment
    private lateinit var viewModel: FormViewModel

    private val renewalViewModel: RenewalViewModel by activityViewModels()
    private var formId: String? = null
    private var userContactId: String = ""
    private var plpSept2Data: PLPLoanStep2Request? = null
    private var plpSept3Data: PLPLoanStep3Request? = null

    // viewmodel de datos de usuario
    private lateinit var userViewModel: UserViewModel

    // campos
    private lateinit var formNombreBanco: EditText
    private lateinit var formCiudadEmpresa: EditText
    private lateinit var formComoEnteraste: EditText
    private lateinit var formNitEmpresa: EditText
    private lateinit var formNombreEmpresa: EditText
    private lateinit var formNombreReferenciaLaboral: EditText
    //private lateinit var formProfesion: EditText
    private lateinit var formReferenciaBancaria: EditText
    private lateinit var formTelefonoEmpresa: EditText
    private lateinit var formTelefonoReferenciaLaboral: EditText
    private lateinit var formTipoContratoLaboral: EditText
    private lateinit var formTipoCuenta: EditText

    private lateinit var formOcupacion: EditText
    private lateinit var errorOcupacion: TextView
    private lateinit var formEmpresa: EditText
    private lateinit var errorEmpresa: TextView
    //private lateinit var formNitEmpresa: EditText
    private lateinit var errorNitEmpresa: TextView
    //private lateinit var formCiudadEmpresa: EditText
    private lateinit var errorCiudadEmpresa: TextView
    //private lateinit var formTelEmpresa: EditText
    private lateinit var errorTelEmpresa: TextView
    //private lateinit var formNoFamilyReferer: EditText
    private lateinit var errorFamilyReferer: TextView
    //private lateinit var formCelNoFamilyReferer: EditText
    private lateinit var errorCelNoFamilyReferer: TextView
    //private lateinit var formContractTypeReferer: EditText
    private lateinit var errorContractTypeReferer: TextView
    //private lateinit var formBankStep3: EditText
    private lateinit var errorBankStep3: TextView
    //private lateinit var formTypeAccountStep3: EditText
    private lateinit var errorTypeAccountStep3: TextView
    //private lateinit var formAccountNumberStep3: EditText
    private lateinit var errorAccountNumberStep3: TextView
    //private lateinit var formServiceStep3: EditText
    private lateinit var errorServiceStep3: TextView
    private lateinit var textViewVerifyInformation: TextView

    private var selectedTipoContratoText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlpStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Opciones tipo contrato
        val contratoMap = mapOf(
            "Fijo" to "Fijo",
            "Indefinido" to "Indefinido",
            "Temporal" to "temporal",
            "Prestación de servicios" to "prestación de servicios"
        )

        // campos
        formNombreBanco = view.findViewById(R.id.form_bank_step3)
        formCiudadEmpresa = view.findViewById(R.id.form_ciudad_empresa)
        formComoEnteraste = view.findViewById(R.id.form_service_step3)
        formNitEmpresa = view.findViewById(R.id.form_nit_empresa)
        formNombreEmpresa = view.findViewById(R.id.form_empresa)
        //formProfesion = view.findViewById(R.id.form_ocupacion)
        formReferenciaBancaria = view.findViewById(R.id.form_account_number_step3)
        formTelefonoEmpresa = view.findViewById(R.id.form_tel_empresa)
        formNombreReferenciaLaboral = view.findViewById(R.id.form_no_family_referer)
        formTelefonoReferenciaLaboral = view.findViewById(R.id.form_cel_no_family_referer)
        formTipoContratoLaboral = view.findViewById(R.id.form_contract_type_referer)
        formTipoCuenta = view.findViewById(R.id.form_type_account_step3)

        formOcupacion = view.findViewById(R.id.form_ocupacion)
        errorOcupacion = view.findViewById(R.id.error_ocupacion)
        //formEmpresa = view.findViewById(R.id.form_empresa)
        errorEmpresa = view.findViewById(R.id.error_empresa)
        //formNitEmpresa = view.findViewById(R.id.form_nit_empresa)
        errorNitEmpresa = view.findViewById(R.id.error_nit_empresa)
        //formCiudadEmpresa = view.findViewById(R.id.form_ciudad_empresa)
        errorCiudadEmpresa = view.findViewById(R.id.error_ciudad_empresa)
        //formTelEmpresa = view.findViewById(R.id.form_tel_empresa)
        errorTelEmpresa = view.findViewById(R.id.error_tel_empresa)
        //formNoFamilyReferer = view.findViewById(R.id.form_no_family_referer)
        errorFamilyReferer = view.findViewById(R.id.error_family_referer)
        //formCelNoFamilyReferer = view.findViewById(R.id.form_cel_no_family_referer)
        errorCelNoFamilyReferer = view.findViewById(R.id.error_cel_no_family_referer)
        //formContractTypeReferer = view.findViewById(R.id.form_contract_type_referer)
        errorContractTypeReferer = view.findViewById(R.id.error_contract_type_referer)
        //formBankStep3 = view.findViewById(R.id.form_bank_step3)
        errorBankStep3 = view.findViewById(R.id.error_bank_step3)
        //formTypeAccountStep3 = view.findViewById(R.id.form_type_account_step3)
        errorTypeAccountStep3 = view.findViewById(R.id.error_type_account_step3r)
        //formAccountNumberStep3 = view.findViewById(R.id.form_account_number_step3)
        errorAccountNumberStep3 = view.findViewById(R.id.error_account_number_step3)
        //formServiceStep3 = view.findViewById(R.id.form_service_step3)
        errorServiceStep3 = view.findViewById(R.id.error_service_step3)
        textViewVerifyInformation = view.findViewById(R.id.textViewVerifyInformation)

        // Obtener ViewModel compartido con la actividad
        val userRepository = UserRepository()
        val preferencesManager = PreferencesManager(requireContext())
        val factory = UserViewModelFactory(userRepository, preferencesManager)
        userViewModel = ViewModelProvider(requireActivity(), factory)[UserViewModel::class.java]
        viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

        binding.formBankStep3.setText(viewModel.banco.value ?: "")

        binding.formBankStep3.addTextChangedListener { editable ->
            viewModel.banco.value = editable?.toString()
        }

        repository = LoanRepository()
        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        val btnNextStep = view.findViewById<Button>(R.id.btnNextStep)
        btnNextStep.setOnClickListener {
            Log.d("RenewalPlpStep3Fragment", "Paso 3")
            //(requireParentFragment() as? RenewalFragment)?.goToNextStep()

            if (validateForm()) {

                var tipoCuenta = ""
                if(formTipoCuenta.text.toString() == "Ahorros"){
                    tipoCuenta = "Cuenta de Ahorros"
                }else if(formTipoCuenta.text.toString() == "Corriente"){
                    tipoCuenta = "Cuenta Corriente"
                }

                // Actualizar datos viewmodel
                if(plpSept3Data != null){
                    val formData = PLPLoanStep3Request(
                        banco = formNombreBanco.text.toString(),
                        ciudadEmpresa = formCiudadEmpresa.text.toString(),
                        comoEnteraste = formComoEnteraste.text.toString(),
                        nitEmpresa = formNitEmpresa.text.toString(),
                        nombreEmpresa = formNombreEmpresa.text.toString(),
                        nombreReferenciaLaboral = formNombreReferenciaLaboral.text.toString(),
                        profesion = formOcupacion.text.toString(),
                        referenciaBancaria = formReferenciaBancaria.text.toString(),
                        telefonoEmpresa = formTelefonoEmpresa.text.toString(),
                        telefonoReferenciaLaboral = formTelefonoReferenciaLaboral.text.toString(),
                        tipoContratoLaboral = contratoMap[selectedTipoContratoText].orEmpty(), //formTipoContratoLaboral.text.toString(),
                        tipoCuenta = tipoCuenta,

                        ciudadDepartamento = plpSept3Data!!.ciudadDepartamento,
                        departamento = plpSept3Data!!.departamento,
                        direccionExacta = plpSept3Data!!.direccionExacta,
                        eps = plpSept3Data!!.eps,
                        formulario = "", // Se asigna luego
                        nombreReferenciaPersonal = plpSept3Data!!.nombreReferenciaPersonal,
                        //numeroTelefonoResidencia = plpSept3Data!!.numeroTelefonoResidencia,  // Variable AMBIENTE TEST
                        numeroTelefonoRecidencia = plpSept3Data!!.numeroTelefonoRecidencia,    // Variable AMBIENTE PROD
                        planCelular = plpSept3Data!!.planCelular,
                        requirioAyuda = plpSept3Data!!.requirioAyuda,
                        salarioReportado = plpSept3Data!!.salarioReportado,
                        telefonoCelular = plpSept3Data!!.telefonoCelular,
                        telefonoReferenciaPersonal = plpSept3Data!!.telefonoReferenciaPersonal,
                        tipoAfiliado = plpSept3Data!!.tipoAfiliado
                    )
                    renewalViewModel.setPlpStep3Request(formData)
                    // Proceder al siguiente paso
                    if(plpSept2Data != null){
                        lifecycleScope.launch {
                            step2()
                        }
                    }else{
                        Log.d("RenewalPlpStep3Fragment", "Datos formulario 2 no cargados: ${plpSept2Data}")
                    }
                }else{
                    Log.d("RenewalPlpStep3Fragment", "Datos formulario 3 no cargados: ${plpSept3Data}")
                }
            }else{
                Log.d("RenewalPlpStep3Fragment", "Errores formulario Paso 3")
            }
        }

        // Observar los datos del usuario
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                userContactId = user.contacto!!
                Log.d("RenewalPlpStep3Fragment", "Contact ID: $userContactId")
            }
        }
        // Fin Observar los datos del usuario

        // Observar formulario paso 2 y paso 3
        renewalViewModel.plpStep2Request.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                plpSept2Data = data
                Log.d("RenewalPlpStep3Fragment", "plpSept2Data data: $plpSept2Data")
            }
        }
        renewalViewModel.plpStep3Request.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                plpSept3Data = data
                Log.d("RenewalPlpStep3Fragment", "plpSept3Data data: $plpSept3Data")
            }
        }

        //editTextContractType = view.findViewById(R.id.form_contract_type_referer)

        //editTextServiceStep3 = view.findViewById(R.id.form_service_step3)

        formTipoContratoLaboral.setOnClickListener {
            //val viaList = listOf("Fijo", "Indefinido", "temporal", "prestación de servicios")
            //showBottomSheetDialogContractType(viaList, formTipoContratoLaboral)

            val visibleList = contratoMap.keys.toList()

            showBottomSheetDialogContractType(visibleList, formTipoContratoLaboral) { selectedText ->
                formTipoContratoLaboral.setText(selectedText)
                selectedTipoContratoText = selectedText
                val text = contratoMap[selectedTipoContratoText].orEmpty()
                //Log.d("ContratoSeleccionado", "Visible: $selectedText, Valor real: $text")
            }
        }
        formComoEnteraste.setOnClickListener {
            val viaList = listOf("Facebook", "Instagram", "Google", "Amigo", "Tik Tok", "Otros")
            showBottomSheetDialogServiceStep3(viaList, formComoEnteraste)
        }

        binding.formBankStep3.setOnClickListener {
            showBankBottomSheet()
        }

        binding.formTypeAccountStep3.setOnClickListener {
            showTypeAccountBottomSheet()
        }

        // Bloquear opción de pagar en campos de cuenta
        disableAccountNumberPaste()
    }

    private fun disableAccountNumberPaste(){
        val numberAccountEditText = formReferenciaBancaria

        // Desactiva la selección y el menú contextual (copiar, pegar, cortar)
        numberAccountEditText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        // También evita el menú emergente de pegar al mantener presionado
        numberAccountEditText.setLongClickable(false)
        numberAccountEditText.isLongClickable = false
        numberAccountEditText.setTextIsSelectable(false)
    }

    private suspend fun step2(){
        val formData = PLPLoanStep2Request(
            contacto = userContactId,
            antiguedadLaboral = plpSept2Data?.antiguedadLaboral!!,
            cantidadDependientes = plpSept2Data?.cantidadDependientes!!,
            cantidadHijos = plpSept2Data?.cantidadHijos!!,
            celular = plpSept2Data?.celular!!,
            correoElectronico = plpSept2Data?.correoElectronico!!,
            fechaExpedicion = plpSept2Data?.fechaExpedicion!!,
            ingresoMensual = plpSept2Data?.ingresoMensual!!,
            nombre = plpSept2Data?.nombre!!,
            numeroDocumento = plpSept2Data?.numeroDocumento!!,
            password = plpSept2Data?.password!!,
            primerApellido = plpSept2Data?.primerApellido!!,
            tipoDocumento = plpSept2Data?.tipoDocumento!!
        )

        Log.d("RenewalPlpStep2Fragment", "STEP 2 form: ${formData}")

        // Mostrar dialog
        loadingDialog.show(parentFragmentManager, "LoadingDialog")

        try {
            val plpLoanStep2Response = withContext(Dispatchers.IO) {
                repository.getDataPlpStep2(formData)
            }

            loadingDialog.dismiss() // ocultar loading

            Log.d("RenewalPlpStep2Fragment", "PLP loan step 2 Data: ${plpLoanStep2Response}")

            if(plpLoanStep2Response != null && plpLoanStep2Response.solicitud.codigo == "200"){

                // Actualizar formulario
                formId = plpLoanStep2Response.solicitud.formulario
                renewalViewModel.setFormId(formId!!)
                Log.d("RenewalPlpStep2Fragment", "Formulario PLP: ${formId}")

                (requireParentFragment() as? RenewalFragment)?.goToNextStep()

            }else{
                Log.d("RenewalPlpStep1Fragment", "Error PLP loan step 2 Data")
                loadingDialog.dismiss() // ocultar loading
            }

            loadingDialog.dismiss() // ocultar loading
        } catch (e: Exception) {
            loadingDialog.dismiss() // Ocultar en caso de error
            Log.e("RenewalPlpStep1Fragment", "Error obteniendo datos", e)
        }
    }

    private fun showBottomSheetDialogContractType(list: List<String>, editText: EditText, onItemSelected: (String) -> Unit) {
        val dialogViewContractType = layoutInflater.inflate(R.layout.bottom_sheet_contract_type, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogViewContractType)
        }
        val recyclerViewPlanCel = dialogViewContractType.findViewById<RecyclerView>(R.id.recyclerViewContractType)
        recyclerViewPlanCel.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPlanCel.adapter = GenericAdapter(list) { selectedItem ->
            //editText.setText(selectedItem)
            onItemSelected(selectedItem)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBottomSheetDialogServiceStep3(list: List<String>, editText: EditText) {
        val dialogViewEPS = layoutInflater.inflate(R.layout.bottom_sheet_service_step3, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogViewEPS)
        }
        val recyclerViewEPS = dialogViewEPS.findViewById<RecyclerView>(R.id.recyclerViewServiceStep3)
        recyclerViewEPS.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewEPS.adapter = GenericAdapter(list) { selectedItem ->
            editText.setText(selectedItem)
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
            binding.formBankStep3.setText(selectedBankName)
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
            binding.formTypeAccountStep3.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Profesión u ocupación (Opcional)
        val ocupacion = formOcupacion.text.toString().trim()
        errorOcupacion.visibility = View.GONE

        // Nombre de la empresa (Opcional)
        val empresa = formNombreEmpresa.text.toString().trim()
        errorEmpresa.visibility = View.GONE

        // NIT de la empresa (Opcional, mínimo 8 caracteres si se ingresa)
        val nitEmpresa = formNitEmpresa.text.toString().trim()
        if (nitEmpresa.isNotEmpty() && nitEmpresa.length < 8) {
            errorNitEmpresa.text = "El NIT debe tener al menos 8 caracteres"
            errorNitEmpresa.visibility = View.VISIBLE
            formNitEmpresa.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            formNitEmpresa.requestFocus()
            isValid = false
        } else {
            errorNitEmpresa.visibility = View.GONE
            formNitEmpresa.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Ciudad de la empresa (Opcional)
        val ciudadEmpresa = formCiudadEmpresa.text.toString().trim()
        errorCiudadEmpresa.visibility = View.GONE

        // Teléfono de la empresa (Opcional, mínimo 8 caracteres si se ingresa)
        val telEmpresa = formTelefonoEmpresa.text.toString().trim()
        if (telEmpresa.isNotEmpty() && telEmpresa.length < 8) {
            errorTelEmpresa.text = "El teléfono debe tener al menos 8 caracteres"
            errorTelEmpresa.visibility = View.VISIBLE
            formTelefonoEmpresa.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            formTelefonoEmpresa.requestFocus()
            isValid = false
        } else {
            errorTelEmpresa.visibility = View.GONE
            formTelefonoEmpresa.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Nombre completo referencia no familiar (Obligatorio)
        isValid = validateField(formNombreReferenciaLaboral, errorFamilyReferer, "Por favor, ingrese el nombre completo") && isValid

        // Teléfono referencia no familiar (Obligatorio, mínimo 8 caracteres)
        val celNoFamily = formTelefonoReferenciaLaboral.text.toString().trim()
        if (celNoFamily.isEmpty()) {
            errorCelNoFamilyReferer.text = "Por favor, ingrese un teléfono de referencia"
            errorCelNoFamilyReferer.visibility = View.VISIBLE
            formTelefonoReferenciaLaboral.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            formTelefonoReferenciaLaboral.requestFocus()
            isValid = false
        } else if (celNoFamily.length < 8) {
            errorCelNoFamilyReferer.text = "El teléfono debe tener al menos 8 caracteres"
            errorCelNoFamilyReferer.visibility = View.VISIBLE
            formTelefonoReferenciaLaboral.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            formTelefonoReferenciaLaboral.requestFocus()
            isValid = false
        } else {
            errorCelNoFamilyReferer.visibility = View.GONE
            formTelefonoReferenciaLaboral.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Tipo de contrato laboral (Obligatorio)
        isValid = validateField(formTipoContratoLaboral, errorContractTypeReferer, "Por favor, seleccione su tipo de contrato") && isValid

        // ¿Cuál es tu banco? (Obligatorio)
        isValid = validateField(formNombreBanco, errorBankStep3, "Por favor, seleccione un banco") && isValid

        // Tipo de cuenta (Obligatorio)
        isValid = validateField(formTipoCuenta, errorTypeAccountStep3, "Por favor, seleccione un tipo de cuenta") && isValid

        // Número de cuenta (Obligatorio, mínimo 11 caracteres)
        val accountNumber = formReferenciaBancaria.text.toString().trim()
        if (accountNumber.isEmpty()) {
            errorAccountNumberStep3.text = "Por favor, ingrese su número de cuenta"
            errorAccountNumberStep3.visibility = View.VISIBLE
            formReferenciaBancaria.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            formReferenciaBancaria.requestFocus()
            isValid = false
        } else if (accountNumber.length < 9) {
            errorAccountNumberStep3.text = "El número de cuenta debe tener al menos 9 caracteres"
            errorAccountNumberStep3.visibility = View.VISIBLE
            formReferenciaBancaria.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            formReferenciaBancaria.requestFocus()
            isValid = false
        } else {
            errorAccountNumberStep3.visibility = View.GONE
            formReferenciaBancaria.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // ¿Cómo te enteraste de nuestros servicios? (Obligatorio)
        isValid = validateField(formComoEnteraste, errorServiceStep3, "Por favor, seleccione una opción") && isValid

        textViewVerifyInformation.visibility = if (isValid) View.GONE else View.VISIBLE

        return isValid
    }

    private fun validateField(editText: EditText, errorView: TextView, errorMessage: String): Boolean {
        return if (editText.text.toString().trim().isEmpty()) {
            errorView.text = errorMessage
            errorView.visibility = View.VISIBLE
            editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            editText.requestFocus()
            false
        } else {
            errorView.visibility = View.GONE
            editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
            true
        }
    }
}