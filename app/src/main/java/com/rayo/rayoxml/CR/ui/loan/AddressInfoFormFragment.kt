package com.rayo.rayoxml.cr.ui.loan

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.models.loadProvinciasFromJson
import com.rayo.rayoxml.cr.adapters.CantonAdapter
import com.rayo.rayoxml.cr.adapters.DistritoAdapter
import com.rayo.rayoxml.cr.adapters.ProvinciaAdapter
import com.rayo.rayoxml.cr.models.Canton
import com.rayo.rayoxml.cr.viewModels.FormViewModel
import com.rayo.rayoxml.databinding.CrDomicilioInfoFormBinding

class AddressInfoFormFragment : Fragment() {

    private var _binding: CrDomicilioInfoFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var editTextProvincia: EditText
    private lateinit var editTextCanton: EditText
    private lateinit var editTextDistrito: EditText

    private val formViewModel: FormViewModel by activityViewModels()
    private val listaProvincias by lazy { loadProvinciasFromJson(requireContext()) }
    private var listaCantones: List<Canton> = emptyList()
    private var listaDistritos: List<String> = emptyList()

    private val DEFAULT_PROVINCIA_TEXT = "Seleccione una provincia"
    private val DEFAULT_CANTON_TEXT = "Seleccione un cantón"
    private val DEFAULT_DISTRITO_TEXT = "Seleccione un distrito"

/*    private lateinit var viewModel: FormViewModel
    private lateinit var editTextVia: EditText
    private lateinit var editTextViaAlterna: EditText*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = CrDomicilioInfoFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Deshabilitar campos cantón y distrito
        updateEditTextStyle(
            context = requireContext(),
            editText = binding.formAddressCanton,
            textColorResId = R.color.woodsmoke_500,
            backgroundColorResId = R.color.woodsmoke_200,
            enabled = false
        )
        updateEditTextStyle(
            context = requireContext(),
            editText = binding.formAddressDistrito,
            textColorResId = R.color.woodsmoke_500,
            backgroundColorResId = R.color.woodsmoke_200,
            enabled = false
        )

        binding.formAddressProvincia.addTextChangedListener { editable ->
            // Habilitar campo cantón
            val text = editable?.toString() ?: ""

            if (text != DEFAULT_PROVINCIA_TEXT) {
                updateEditTextStyle(
                    context = requireContext(),
                    editText = binding.formAddressCanton,
                    textColorResId = R.color.woodsmoke_900,
                    backgroundColorResId = R.color.pale_sky_50,
                    enabled = true
                )
            }
        }

        binding.formAddressCanton.addTextChangedListener { editable ->
            // Habilitar campo distrito
            val text = editable?.toString() ?: ""

            if (text != DEFAULT_CANTON_TEXT) {
                updateEditTextStyle(
                    context = requireContext(),
                    editText = binding.formAddressDistrito,
                    textColorResId = R.color.woodsmoke_900,
                    backgroundColorResId = R.color.pale_sky_50,
                    enabled = true
                )
            }
        }

/*       viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

        editTextVia = view.findViewById(R.id.form_address_via)
        editTextViaAlterna = view.findViewById(R.id.form_address_carrer)

        editTextVia.setOnClickListener {
            showBottomSheetDialogVia()
        }

        editTextViaAlterna.setOnClickListener {
            showBottomSheetDialogViaAlter()
        }*/

        editTextProvincia = view.findViewById(R.id.form_address_provincia)
        editTextCanton = view.findViewById(R.id.form_address_canton)
        editTextDistrito = view.findViewById(R.id.form_address_distrito)

        editTextProvincia.setOnClickListener {
            showBottomSheetDialogProvincia()
        }
        editTextCanton.setOnClickListener {
            if(listaCantones.isNotEmpty()){
                showBottomSheetDialogCanton(listaCantones)
            }

        }
        editTextDistrito.setOnClickListener {
            if(listaDistritos.isNotEmpty()){
                showBottomSheetDialogDistrito(listaDistritos)
            }
        }

        // Reset listados al cambiar provincia/distrito
        editTextProvincia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.formAddressCanton.setText(DEFAULT_CANTON_TEXT)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        editTextCanton.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.formAddressDistrito.setText(DEFAULT_DISTRITO_TEXT)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnNextStep.setOnClickListener {

            if (validateForm()) {

                val editTextProvincia = binding.formAddressProvincia
                val editTextCanton = binding.formAddressCanton
                val editTextDistrito = binding.formAddressDistrito
                val editTextDireccion = binding.formAddressFull

                // Actualizar datos
                formViewModel.updateLocationInfo(
                    provincia = editTextProvincia.text.toString().trim(),
                    canton = editTextCanton.text.toString().trim(),
                    distrito = editTextDistrito.text.toString().trim(),
                    direccionExacta = editTextDireccion.text.toString().trim()
                )

                // Continuar
                (parentFragment as? FormFragment)?.goToNextStep()
            }

            /*val via = binding.formAddressVia.text.toString()
            val number = binding.formAddressNumber.text.toString()
            val carrer = binding.formAddressCarrer.text.toString()
            val complement = binding.formAddressComplement.text.toString()
            val detail = binding.formAddressDetail.text.toString()

            val bundle = bundleOf(
                "goToStep2" to true,
                "via" to via,
                "number" to number,
                "carrer" to carrer,
                "complement" to complement,
                "detail" to detail
            )*/
            /*findNavController().navigate(R.id.action_addressInfoFormFragment_to_formFragment)*/
        }
/*        binding.backToolbarAddressIcon.setOnClickListener {
            val bundle = bundleOf(
                "goToStep2" to true,
            )
            findNavController().navigate(R.id.action_addressInfoFormFragment_to_formFragment, bundle)
        }*/
/*
        binding.toolbarLayoutIconAddress?.setOnClickListener{
            showInfoAddressDialog()
        }*/
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

    private fun showBottomSheetDialogProvincia() {
        //Log.d("AddressInfoFormFragment", "Provincias: ${listaProvincias}")
        editTextProvincia.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_provincia, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextProvincia.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewProvincia)
        val search = dialogView.findViewById<EditText>(R.id.searchProvincia)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener { editable ->
            val text = editable?.toString()?.lowercase() ?: ""
            val filteredList = listaProvincias.filter { it.provincia.lowercase().contains(text) }
            recyclerView.adapter = ProvinciaAdapter(filteredList) { selectedProvincia ->
                binding.formAddressProvincia.setText(selectedProvincia.provincia)
                listaCantones = selectedProvincia.cantones
                Log.d("AddressInfoFormFragment", "Cantones: ${listaCantones}")
                dialog.dismiss()
            }
        }
        recyclerView.adapter = ProvinciaAdapter(listaProvincias.map{it}) { selectedProvincia ->
            binding.formAddressProvincia.setText(selectedProvincia.provincia)
            listaCantones = selectedProvincia.cantones
            Log.d("AddressInfoFormFragment", "Cantones: ${listaCantones}")
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextProvincia.isEnabled = true
        }
    }

    private fun showBottomSheetDialogCanton(cantones: List<Canton>) {
        editTextCanton.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_canton, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextCanton.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCanton)
        val search = dialogView.findViewById<EditText>(R.id.searchCanton)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener { editable ->
            val text = editable?.toString()?.lowercase() ?: ""
            val filteredList = cantones.filter { it.nombre.lowercase().contains(text) }
            recyclerView.adapter = CantonAdapter(filteredList) { selectedCanton ->
                binding.formAddressCanton.setText(selectedCanton.nombre)
                listaDistritos = selectedCanton.distritos
                dialog.dismiss()
            }
        }
        recyclerView.adapter = CantonAdapter(cantones) { selectedCanton ->
            binding.formAddressCanton.setText(selectedCanton.nombre)
            listaDistritos = selectedCanton.distritos
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextCanton.isEnabled = true
        }
    }

    private fun showBottomSheetDialogDistrito(distritos: List<String>) {
        editTextDistrito.isEnabled = false
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_distrito, null)
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
            setOnDismissListener {
                editTextDistrito.isEnabled = true
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewDistrito)
        val search = dialogView.findViewById<EditText>(R.id.searchDistrito)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        search.addTextChangedListener { editable ->
            val text = editable?.toString()?.lowercase() ?: ""
            val filteredList = distritos.filter { it.lowercase().contains(text) }
            recyclerView.adapter = DistritoAdapter(filteredList) { selectedType ->
                binding.formAddressDistrito.setText(selectedType)
                dialog.dismiss()
            }
        }
        recyclerView.adapter = DistritoAdapter(distritos) { selectedType ->
            binding.formAddressDistrito.setText(selectedType)
            dialog.dismiss()
        }

        try {
            dialog.show()
        } catch (e: Exception) {
            editTextDistrito.isEnabled = true
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validaciones
        val editTextProvincia = binding.formAddressProvincia
        val editTextCanton = binding.formAddressCanton
        val editTextDistrito = binding.formAddressDistrito
        val editTextDireccion = binding.formAddressFull

        // Mensajes de error para los campos
        val errorProvincia = binding.errorProvincia
        val errorCanton = binding.errorCanton
        val errorDistrito = binding.errorDistrito
        val errorDireccion = binding.errorAddress

        // Ocultar todos los mensajes de error al inicio
        errorProvincia.visibility = View.GONE
        errorCanton.visibility = View.GONE
        errorDistrito.visibility = View.GONE
        errorDireccion.visibility = View.GONE

        // Validar provincia
        val opcionProvincia = editTextProvincia.text.toString()
        if (opcionProvincia.trim().isEmpty()) {
            errorProvincia.text = "Por favor, seleccione una provincia"
            errorProvincia.visibility = View.VISIBLE
            editTextProvincia.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            editTextProvincia.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar canton
        val opcionCanton = editTextCanton.text.toString()
        if (opcionCanton.trim().isEmpty()) {
            errorCanton.text = "Por favor, seleccione un cantón"
            errorCanton.visibility = View.VISIBLE
            editTextCanton.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            editTextCanton.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar distrito
        val opcionDistrito = editTextDistrito.text.toString()
        if (opcionDistrito.trim().isEmpty()) {
            errorDistrito.text = "Por favor, seleccione un distrito"
            errorDistrito.visibility = View.VISIBLE
            editTextDistrito.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            editTextDistrito.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        // Validar dirección
        if (editTextDireccion.text.toString().trim().isEmpty()) {
            errorDireccion.text = "Por favor, ingrese su dirección exacta"
            errorDireccion.visibility = View.VISIBLE
            editTextDireccion.background = ContextCompat.getDrawable(requireContext(), R.drawable.custom_input_background_error)
            isValid = false
        } else {
            editTextDireccion.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_edit_text)
        }

        return isValid
    }

/*    private fun showInfoAddressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_info_address, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogView.findViewById<TextView>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBottomSheetDialogVia() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_via, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewVia)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ViaAdapter { selectedType ->
            binding.formAddressVia.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showBottomSheetDialogViaAlter() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_via_alter, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewViaAlter)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ViaAlterAdapter { selectedType ->
            binding.formAddressCarrer.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }*/
}