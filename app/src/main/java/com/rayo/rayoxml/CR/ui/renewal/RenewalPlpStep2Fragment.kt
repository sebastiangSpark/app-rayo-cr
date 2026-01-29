package com.rayo.rayoxml.cr.ui.renewal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.adapters.DepartmentAdapter
import com.rayo.rayoxml.cr.adapters.GenericAdapter
import com.rayo.rayoxml.databinding.FragmentPlpStep2Binding
import com.rayo.rayoxml.cr.models.loadDepartamentosFromJson
import com.rayo.rayoxml.cr.services.Loan.LoanRepository
import com.rayo.rayoxml.cr.services.Loan.PLPLoanStep3Request
import com.rayo.rayoxml.cr.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.cr.viewModels.RenewalViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class RenewalPlpStep2Fragment : Fragment() {

    private var _binding: FragmentPlpStep2Binding? = null
    private val binding get() = _binding!!
    private lateinit var repository: LoanRepository
    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment
    private val listaDepartamentos by lazy { loadDepartamentosFromJson(requireContext()) }
    private var ciudadesDisponibles: List<String> = emptyList()
    private lateinit var editTextPlanCel: EditText
    private lateinit var editTextEPS: EditText
    private lateinit var editTextTypeAfiliado: EditText
    private lateinit var editTextAgente: EditText

    private val renewalViewModel: RenewalViewModel by activityViewModels()
    private var formId: String? = null

    // campos
    private lateinit var formUserNameReferer: EditText
    private lateinit var errorUserNameReferer: TextView
    private lateinit var formUserPhoneReferer: EditText
    private lateinit var errorUserPhoneReferer: TextView
    private lateinit var formUserDepartmentReferer: EditText
    private lateinit var errorUserDepartmentReferer: TextView
    private lateinit var formCiudadReferer: EditText
    private lateinit var errorCiudadReferer: TextView
    private lateinit var formDireccionReferer: EditText
    private lateinit var errorDireccionReferer: TextView
    private lateinit var formTelReferer: EditText
    private lateinit var errorTelReferer: TextView
    private lateinit var formCelReferer: EditText
    private lateinit var errorCelReferer: TextView
    private lateinit var formPlanCelReferer: EditText
    private lateinit var errorPlanCelReferer: TextView
    private lateinit var formSalarioReferer: EditText
    private lateinit var errorSalarioReferer: TextView
    private lateinit var formEpsReferer: EditText
    private lateinit var errorEpsReferer: TextView
    private lateinit var formAfiliadoTypeReferer: EditText
    private lateinit var errorAfiliadoTypeReferer: TextView
    private lateinit var formAgenteReferer: EditText
    private lateinit var errorAgenteReferer: TextView
    private lateinit var textViewVerifyInformation: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlpStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // campos
        formUserNameReferer = view.findViewById(R.id.form_user_name_referer)
        errorUserNameReferer = view.findViewById(R.id.error_user_name_referer)
        formUserPhoneReferer = view.findViewById(R.id.form_user_phone_referer)
        errorUserPhoneReferer = view.findViewById(R.id.error_user_phone_referer)
        formUserDepartmentReferer = view.findViewById(R.id.form_user_department_referer)
        errorUserDepartmentReferer = view.findViewById(R.id.error_user_department_referer)
        formCiudadReferer = view.findViewById(R.id.form_ciudad_referer)
        errorCiudadReferer = view.findViewById(R.id.error_ciudad_referer)
        formDireccionReferer = view.findViewById(R.id.form_dirección_referer)
        errorDireccionReferer = view.findViewById(R.id.error_dirección_referer)
        formTelReferer = view.findViewById(R.id.form_tel_referer)
        errorTelReferer = view.findViewById(R.id.error_tel_referer)
        formCelReferer = view.findViewById(R.id.form_cel_referer)
        errorCelReferer = view.findViewById(R.id.error_cel_referer)
        formPlanCelReferer = view.findViewById(R.id.form_plan_cel_referer)
        errorPlanCelReferer = view.findViewById(R.id.error_plan_cel_referer)
        formSalarioReferer = view.findViewById(R.id.form_salario_referer)
        errorSalarioReferer = view.findViewById(R.id.error_salario_referer)
        formEpsReferer = view.findViewById(R.id.form_EPS_referer)
        errorEpsReferer = view.findViewById(R.id.error_EPS_referer)
        formAfiliadoTypeReferer = view.findViewById(R.id.form_afiliado_type_referer)
        errorAfiliadoTypeReferer = view.findViewById(R.id.error_afiliado_type_referer)
        formAgenteReferer = view.findViewById(R.id.form_agente_referer)
        errorAgenteReferer = view.findViewById(R.id.error_agente_referer)
        textViewVerifyInformation = view.findViewById(R.id.textViewVerifyInformation)

        repository = LoanRepository()
        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()

        val btnNextStep = view.findViewById<Button>(R.id.btnNextStep)
        btnNextStep.setOnClickListener {
            Log.d("RenewalPlpStep2Fragment", "Paso 2")
            if (validateForm()) {
                // Actualizar datos viewmodel
                val formData = PLPLoanStep3Request(
                    banco = "",
                    ciudadEmpresa = "",
                    comoEnteraste = "",
                    nitEmpresa = "",
                    nombreEmpresa =  "",
                    nombreReferenciaLaboral = "",
                    profesion = "",
                    referenciaBancaria = "",
                    telefonoEmpresa = "",
                    telefonoReferenciaLaboral = "",
                    tipoContratoLaboral = "",
                    tipoCuenta = "",
                    ciudadDepartamento = formUserDepartmentReferer.text.toString(),
                    departamento = formUserDepartmentReferer.text.toString(),
                    direccionExacta = formDireccionReferer.text.toString(),
                    eps = formEpsReferer.text.toString(),
                    formulario = "", // Se asigna luego
                    nombreReferenciaPersonal = formUserNameReferer.text.toString(),
                    numeroTelefonoResidencia = formTelReferer.text.toString(),
                    planCelular = formPlanCelReferer.text.toString(),
                    requirioAyuda = formAgenteReferer.text.toString(),
                    salarioReportado = formSalarioReferer.text.toString(),
                    telefonoCelular = formCelReferer.text.toString(),
                    telefonoReferenciaPersonal = formUserPhoneReferer.text.toString(),
                    tipoAfiliado = formAfiliadoTypeReferer.text.toString()
                )
                renewalViewModel.setPlpStep3Request(formData)
                // Proceder al siguiente paso
                (requireParentFragment() as? RenewalFragment)?.goToNextStep()
            }else{
                Log.d("RenewalPlpStep2Fragment", "Errores formulario Paso 2")
            }
        }
        //editTextPlanCel = view.findViewById(R.id.form_plan_cel_referer)
        //editTextEPS = view.findViewById(R.id.form_EPS_referer)
        //editTextTypeAfiliado = view.findViewById(R.id.form_afiliado_type_referer)
        //editTextAgente = view.findViewById(R.id.form_agente_referer)

        binding.formUserDepartmentReferer.setOnClickListener {
            showDepartamentoBottomSheet()
        }

        formPlanCelReferer.setOnClickListener {
            val viaList = listOf("prepago", "postpago")
            showBottomSheetDialog(viaList, formPlanCelReferer)
        }
        formEpsReferer.setOnClickListener {
            val viaList = listOf("Suramericana S.A.", "Aliansalud EPS S.A.", "Sanitas S.A. EPS", "Compensar EPS", "Salud Total S.A. EPS", "Nueva EPS", "Coomeva EPS S.A.", "EPS Famisanar LTDA", "Serv. Occ. de salud SOS EPS","Comfenalco Valle EPS")
            showBottomSheetDialogEPS(viaList, formEpsReferer)
        }
        formAfiliadoTypeReferer.setOnClickListener {
            val viaList = listOf("BENEFICIARIO", "COTIZANTE")
            showBottomSheetDialogType(viaList, formAfiliadoTypeReferer)
        }
        formAgenteReferer.setOnClickListener {
            val viaList = listOf("Si", "No")
            showBottomSheetDialogAgente(viaList, formAgenteReferer)
        }
    }

    private fun showDepartamentoBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_departamentos, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerDepartamentos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = DepartmentAdapter(listaDepartamentos.map { it.departamento }) { selectedDepartamentoName ->
            binding.formUserDepartmentReferer.setText(selectedDepartamentoName)
            val selectedDepartamento = listaDepartamentos.find { it.departamento == selectedDepartamentoName }
            ciudadesDisponibles = selectedDepartamento?.municipios ?: emptyList()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBottomSheetDialog(list: List<String>, editText: EditText) {
        val dialogViewPlanCel = layoutInflater.inflate(R.layout.bottom_sheet_plan_cel, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogViewPlanCel)
        }
        val recyclerViewPlanCel = dialogViewPlanCel.findViewById<RecyclerView>(R.id.recyclerViewPlanCel)
        recyclerViewPlanCel.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewPlanCel.adapter = GenericAdapter(list) { selectedItem ->
            editText.setText(selectedItem)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBottomSheetDialogEPS(list: List<String>, editText: EditText) {
        val dialogViewEPS = layoutInflater.inflate(R.layout.bottom_sheet_eps, null)
        val dialog2 = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogViewEPS)
        }
        val recyclerViewEPS = dialogViewEPS.findViewById<RecyclerView>(R.id.recyclerViewEPS)
        recyclerViewEPS.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewEPS.adapter = GenericAdapter(list) { selectedItem ->
            editText.setText(selectedItem)
            dialog2.dismiss()
        }
        dialog2.show()
    }
    private fun showBottomSheetDialogType(list: List<String>, editText: EditText) {
        val dialogViewAfiliadoType = layoutInflater.inflate(R.layout.bottom_sheet_afiliado_type, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogViewAfiliadoType)
        }
        val recyclerViewAfiliadoType = dialogViewAfiliadoType.findViewById<RecyclerView>(R.id.recyclerViewAfiliadoType)
        recyclerViewAfiliadoType.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAfiliadoType.adapter = GenericAdapter(list) { selectedItem ->
            editText.setText(selectedItem)
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun showBottomSheetDialogAgente(list: List<String>, editText: EditText) {
        val dialogViewAgente = layoutInflater.inflate(R.layout.bottom_sheet_agente, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogViewAgente)
        }
        val recyclerViewAgente = dialogViewAgente.findViewById<RecyclerView>(R.id.recyclerViewAgente)
        recyclerViewAgente.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAgente.adapter = GenericAdapter(list) { selectedItem ->
            editText.setText(selectedItem)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        isValid = validateField(formUserNameReferer, errorUserNameReferer, "Ingrese nombre de referencia personal") && isValid
        isValid = validatePhoneField(formUserPhoneReferer, errorUserPhoneReferer, "Ingrese un teléfono válido") && isValid
        isValid = validateField(formUserDepartmentReferer, errorUserDepartmentReferer, "Seleccione un departamento") && isValid
        isValid = validateField(formCiudadReferer, errorCiudadReferer, "Ingrese una ciudad") && isValid
        isValid = validateField(formDireccionReferer, errorDireccionReferer, "Ingrese la dirección exacta") && isValid
        isValid = validatePhoneField(formTelReferer, errorTelReferer, "Ingrese un teléfono válido") && isValid
        isValid = validatePhoneField(formCelReferer, errorCelReferer, "Ingrese un celular válido") && isValid
        isValid = validateField(formPlanCelReferer, errorPlanCelReferer, "Seleccione el plan celular") && isValid
        isValid = validateSalaryField(formSalarioReferer, errorSalarioReferer, "Ingrese un salario válido") && isValid
        isValid = validateField(formEpsReferer, errorEpsReferer, "Seleccione su EPS") && isValid
        isValid = validateField(formAfiliadoTypeReferer, errorAfiliadoTypeReferer, "Seleccione tipo de afiliado") && isValid
        isValid = validateField(formAgenteReferer, errorAgenteReferer, "Seleccione una opción") && isValid

        textViewVerifyInformation.visibility = if (isValid) View.GONE else View.VISIBLE
        return isValid
    }

    private fun validateField(editText: EditText, errorTextView: TextView, errorMessage: String): Boolean {
        return if (editText.text.isNullOrEmpty()) {
            errorTextView.text = errorMessage
            errorTextView.visibility = View.VISIBLE
            false
        } else {
            errorTextView.visibility = View.GONE
            true
        }
    }

    private fun validatePhoneField(editText: EditText, errorTextView: TextView, errorMessage: String): Boolean {
        val text = editText.text.toString()
        return if (text.length < 7) {
            errorTextView.text = errorMessage
            errorTextView.visibility = View.VISIBLE
            false
        } else {
            errorTextView.visibility = View.GONE
            true
        }
    }

    private fun validateSalaryField(editText: EditText, errorTextView: TextView, errorMessage: String): Boolean {
        val text = editText.text.toString()
        return if (text.isEmpty() || text.toIntOrNull() ?: 0 <= 0) {
            errorTextView.text = errorMessage
            errorTextView.visibility = View.VISIBLE
            false
        } else {
            errorTextView.visibility = View.GONE
            true
        }
    }

}
