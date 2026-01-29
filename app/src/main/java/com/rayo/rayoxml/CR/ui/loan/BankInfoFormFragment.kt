package com.rayo.rayoxml.cr.ui.loan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.adapters.EconomyActivityAdapter
import com.rayo.rayoxml.cr.adapters.ProfesionAdapter
import com.rayo.rayoxml.cr.adapters.HappinessAdapter
import com.rayo.rayoxml.cr.adapters.MonedaAdapter
import com.rayo.rayoxml.cr.models.loadActividadesEconomicasiasFromJson
import com.rayo.rayoxml.cr.models.loadProfesionesFromJson
import com.rayo.rayoxml.cr.services.Auth.AuthRepository
import com.rayo.rayoxml.cr.services.Loan.LoanRepository
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.cr.ui.loading.LoadingDialogFragment
import com.rayo.rayoxml.cr.viewModels.FormViewModel
import com.rayo.rayoxml.databinding.CrEconomyInfoFormBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.matches


interface BankInfoNavigationListener {
    fun onNavigateToAddressInfo()
}

class BankInfoFormFragment(private val userParamsViewModel: UserViewModel) : Fragment() {

    private var _binding: CrEconomyInfoFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var editTextCuenta: EditText
    private lateinit var editTextMoneda: EditText
    private lateinit var editTextEconomicActivity: EditText
    private lateinit var editTextProfesion: EditText
    private lateinit var editTextHapiness: EditText
    private lateinit var editTextSalario: EditText
    private lateinit var editRbCcss: RadioGroup
    private lateinit var editRbReferrer: RadioGroup
    private lateinit var editReferrerCode: EditText
    private lateinit var editReferrerName: EditText

    private val formViewModel: FormViewModel by activityViewModels()
    private val listaActividadesEconomicas by lazy {
        loadActividadesEconomicasiasFromJson(
            requireContext()
        )
    }
    private val listaProfesiones by lazy { loadProfesionesFromJson(requireContext()) }
    private val listHapiness = listOf(
        "Anchetas.",
        "Combos dobles para cine.",
        "Viaje para dos.",
        "Spa doble.",
        "Participar en la rifa de un carro.",
        "Televisor de ultima generación.",
        "Cena para dos.",
        "Entradas para el estadio.",
        "Entradas a conciertos.",
        "Entradas a parque de diversión."
    )
    private val listaMonedas = listOf("Colones", "Dólares")

    private val authRepository = AuthRepository()
    private val loanRepository = LoanRepository()

    // Loading
    private lateinit var loadingDialog: LoadingDialogFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = CrEconomyInfoFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el modal
        loadingDialog = LoadingDialogFragment()
        var ccssOption: String? = null
        var referrerOption: String? = null
        binding.btnNextStep.setOnClickListener {

            if (validateForm()) {

                // Actualizar datos
                formViewModel.updateEconomicInfo(
                    cuentaIBAN = editTextCuenta.text.toString().trim(),
                    moneda = editTextMoneda.text.toString().trim(),
                    salario = editTextSalario.text.toString().trim().toInt(),
                    actividadEconomica = editTextEconomicActivity.text.toString().trim(),
                    profesion = editTextProfesion.text.toString().trim(),
                    fidelidad = editTextHapiness.text.toString().trim(),
                    ccss = ccssOption ?: "",
                    referrer = referrerOption ?: "",
                    referrerCode = if (editRbReferrer.checkedRadioButtonId == R.id.rbRefererYes) {
                        editReferrerCode.text.toString().trim()
                    } else null,
                    referrerName = if (editRbReferrer.checkedRadioButtonId == R.id.rbRefererYes) {
                        editReferrerName.text.toString().trim()
                    } else null
                )
                // Mostrar dialog
                loadingDialog.show(parentFragmentManager, "LoadingDialog")

                try {
                    // Obtener token
                    lifecycleScope.launch {
                        val authResponse = authRepository.getToken()
                        if (authResponse != null && authResponse.accessToken.isNotEmpty() && ccssOption == "Si") {
                            Log.d("BankInfoFormFragment", "Auth Data: ${authResponse}")
                            // Asignar token temporal
                            formViewModel.setAuthToken(authResponse.accessToken)

                            // Enviar datos
                            if (formViewModel.loanRequest.value != null) {
                                val createLoanResponse = withContext(Dispatchers.IO) {
                                    loanRepository.createLoan(
                                        authResponse.accessToken,
                                        formViewModel.loanRequest.value!!
                                    )
                                }
                                loadingDialog.dismiss()

                                Log.d("BankInfoFormFragment CR", "Datos api: ${createLoanResponse}")
                                if (createLoanResponse != null) {
                                    if (createLoanResponse.mensaje == "Solicitud creada correctamente") {
                                        // Asignar datos de propuesta
                                        formViewModel.setLoanProposal(createLoanResponse)
                                        // Continuar
                                        (parentFragment as? FormFragment)?.goToNextStep()
                                    } else {
                                        // Mensaje de error
                                        val errorGeneral = binding.errorGeneral
                                        errorGeneral.visibility = View.VISIBLE
                                        if (createLoanResponse.mensaje == "Error al crear la solicitud - El número de teléfono o celular proporcionado no es válido. -- ()") {
                                            errorGeneral.text =
                                                "El número de teléfono o celular proporcionado no es válido"
                                            return@launch
                                        }
                                        if (createLoanResponse.mensaje.contains("DUPLICATES_DETECTED")) {
                                            errorGeneral.text =
                                                "El número de cédula proporcionado ya se encuentra registrado en el sistema."
                                            return@launch
                                        }
                                        errorGeneral.text = createLoanResponse.mensaje
                                    }
                                } else {
                                    Log.d("BankInfoFormFragment CR", "createLoanResponse null")
                                }
                            }
                        } else {
                            // Pantalla de rechazo
                            val bundle = Bundle().apply {
                                putBoolean("shouldReject", true)
                            }
                            findNavController().navigate(R.id.disbursementApprovalFragment, bundle)
                        }
                    }
                } catch (e: Exception) {
                    loadingDialog.dismiss() // Ocultar en caso de error
                    Log.e("BankInfoFormFragment CR", "Error obteniendo datos", e)
                }

            }
        }

        editTextCuenta = binding.formNumberAccountIBAN
        editTextMoneda = binding.formMoney
        editTextEconomicActivity = binding.formActivityEconomic
        editTextProfesion = binding.formProfesion
        editTextHapiness = binding.formHappiness
        editTextSalario = binding.formSalary
        editRbCcss = binding.radioGroupCCSS
        editRbReferrer = binding.radioGroupReferrer
        editReferrerCode = binding.formRefererCode
        editReferrerName = binding.formRefererName

        editRbCcss.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val radioButton: RadioButton = group.findViewById(checkedId)
            ccssOption = radioButton.text.toString()
            editTextCuenta.clearFocus()
            editTextSalario.clearFocus()
        }

        editRbReferrer.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val radioButton: RadioButton = group.findViewById(checkedId)
            referrerOption = radioButton.text.toString()
            editTextCuenta.clearFocus()
            editTextSalario.clearFocus()
            if (radioButton.text.toString() == "Si") {
                binding.llReferer.visibility = View.VISIBLE
            } else {
                binding.llReferer.visibility = View.GONE
            }
        }

        // Cargar opción por defecto en moneda
        if (editTextMoneda.text.isNullOrBlank()) {
            editTextMoneda.setText(listaMonedas.first())
        }

        editTextMoneda.setOnClickListener {
            showBottomSheetDialogMoneda()
        }

        editTextEconomicActivity.setOnClickListener {
            showBottomSheetDialogEconomyActivity()
        }

        editTextProfesion.setOnClickListener {
            showBottomSheetDialogProfesion()
        }

        editTextHapiness.setOnClickListener {
            showBottomSheetDialogHapiness()
        }
    }

    private fun showBottomSheetDialogMoneda() {
        editTextMoneda.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_moneda, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextMoneda.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewMoneda)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MonedaAdapter(listaMonedas) { selectedType ->
            binding.formMoney.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextMoneda.isEnabled = true // Re-enable on error
            // Handle or log exception
        }
    }

    private fun showBottomSheetDialogEconomyActivity() {
        editTextEconomicActivity.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_economy_activity, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextEconomicActivity.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewEconomyActivity)
        val search = dialogView.findViewById<EditText>(R.id.searchEconomyActivity)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener { editable ->
            val text = editable?.toString()?.lowercase() ?: ""
            val filteredList = listaActividadesEconomicas.filter {
                it.name.lowercase().contains(text) || it.code.toString().contains(text)
            }
            recyclerView.adapter = EconomyActivityAdapter(filteredList) { selectedType ->
                binding.formActivityEconomic.setText(selectedType)
                dialog.dismiss()
            }
        }
        recyclerView.adapter = EconomyActivityAdapter(listaActividadesEconomicas) { selectedType ->
            binding.formActivityEconomic.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextEconomicActivity.isEnabled = true
        }
    }

    private fun showBottomSheetDialogProfesion() {
        editTextProfesion.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_profesion, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextProfesion.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewProfesion)
        val search = dialogView.findViewById<EditText>(R.id.searchProfesion)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener { editable ->
            val text = editable?.toString()?.lowercase() ?: ""
            val filteredList = listaProfesiones.filter {
                it.name.lowercase().contains(text) || it.code.toString().contains(text)
            }
            recyclerView.adapter = ProfesionAdapter(filteredList) { selectedType ->
                binding.formProfesion.setText(selectedType)
                dialog.dismiss()
            }
        }
        recyclerView.adapter = ProfesionAdapter(listaProfesiones) { selectedType ->
            binding.formProfesion.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextProfesion.isEnabled = true
        }
    }

    private fun showBottomSheetDialogHapiness() {
        editTextHapiness.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_hapiness, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextHapiness.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewHapiness)
        val search = dialogView.findViewById<EditText>(R.id.searchHapiness)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener { editable ->
            val text = editable?.toString()?.lowercase() ?: ""
            val filteredList = listHapiness.filter {
                it.lowercase().contains(text)
            }
            recyclerView.adapter = HappinessAdapter(filteredList) { selectedType ->
                binding.formHappiness.setText(selectedType)
                dialog.dismiss()
            }
        }
        recyclerView.adapter = HappinessAdapter(listHapiness) { selectedType ->
            binding.formHappiness.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextHapiness.isEnabled = true
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val errorCuenta = binding.errorAccountNumberIBAN
        val errorMoneda = binding.errorMoney
        val errorEconomicActivity = binding.errorActivityEconomic
        val errorProfesion = binding.errorProfesion
        val errorHapiness = binding.errorHappiness
        val errorSalario = binding.errorSalary
        val errorCcss = binding.errorCcss
        val errorReferrer = binding.errorReferer
        val errorReferrerCode = binding.errorRefererCode
        val errorReferrerName = binding.errorRefererName

        val errorGeneral = binding.errorGeneral
        errorGeneral.visibility = View.GONE

        // Ocultar todos los mensajes de error al inicio
        hideError(errorCuenta, editTextCuenta)
        hideError(errorMoneda, editTextMoneda)
        hideError(errorEconomicActivity, editTextEconomicActivity)
        hideError(errorProfesion, editTextProfesion)
        hideError(errorHapiness, editTextHapiness)
        hideError(errorSalario, editTextSalario)
        hideError(errorReferrerCode, editReferrerCode)
        hideError(errorReferrerName, editReferrerName)

        // Validar cuenta
        val opcionCuenta = editTextCuenta.text.toString().trim()
        val regexPatternIBAN = "^[A-Za-z]{2}[0-9]+$".toRegex()
        if (opcionCuenta.isEmpty()) {
            showError(errorCuenta, editTextCuenta, "Por favor, ingrese su cuenta IBAN")
            isValid = false
        }
        if (opcionCuenta.length < 22) {
            showError(
                errorCuenta,
                editTextCuenta,
                "La cuenta IBAN debe tener 22 caracteres"
            )
            isValid = false
        }
        if (opcionCuenta.matches(regexPatternIBAN).not()) {
            showError(
                errorCuenta,
                editTextCuenta,
                "El formato de la cuenta IBAN es incorrecto. Ejemplo: CR12345678901234567890"
            )
            isValid = false
        }

        // Validar salario
        val salario = editTextSalario.text.toString()
        if (salario.trim().isEmpty() || salario.toInt() < 1) {
            showError(errorSalario, editTextSalario, "Por favor, ingrese el salario mensual")
            isValid = false
        }

        // Validar activ econ
        val opcionActividad = editTextEconomicActivity.text.toString()
        if (opcionActividad.trim().isEmpty()) {
            showError(
                errorEconomicActivity,
                editTextEconomicActivity,
                "Por favor, seleccione su actividad económica"
            )
            isValid = false
        }

        // Validar profesión
        val opcionProfesion = editTextProfesion.text.toString()
        if (opcionProfesion.trim().isEmpty()) {
            showError(errorProfesion, editTextProfesion, "Por favor, seleccione su profesión")
            isValid = false
        }

        // Validar opción fidelidad
        val opcionFidelidad = editTextHapiness.text.toString()
        if (opcionFidelidad.trim().isEmpty()) {
            showError(errorHapiness, editTextHapiness, "Por favor, seleccione una opción")
            isValid = false
        }

        if (editRbCcss.checkedRadioButtonId == -1) {
            errorCcss.text = "Por favor, seleccione una opción"
            errorCcss.visibility = View.VISIBLE
            isValid = false
        } else {
            errorCcss.visibility = View.GONE
        }

        if (editRbReferrer.checkedRadioButtonId == -1) {
            errorReferrer.text = "Por favor, seleccione una opción"
            errorReferrer.visibility = View.VISIBLE
            isValid = false
        } else {
            errorReferrer.visibility = View.GONE
        }

        if (editReferrerCode.text.toString().isEmpty() &&
            editRbReferrer.checkedRadioButtonId == R.id.rbRefererYes
        ) {
            showError(
                errorReferrerCode,
                editReferrerCode,
                "Por favor, ingrese el código del referidor"
            )
            isValid = false
        } else {
            hideError(errorReferrer, editReferrerCode)
        }

        if (editReferrerName.text.toString().isEmpty() &&
            editRbReferrer.checkedRadioButtonId == R.id.rbRefererYes
        ) {
            showError(
                errorReferrerName,
                editReferrerName,
                "Por favor, ingrese el nombre del referidor"
            )
            isValid = false
        } else {
            hideError(errorReferrer, editReferrerName)
        }

        return isValid
    }

    private fun showError(errorTextView: TextView, editText: EditText, message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        editText.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
    }

    private fun hideError(errorTextView: TextView, editText: EditText) {
        errorTextView.visibility = View.GONE
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
    }

}