package com.rayo.rayoxml.cr.ui.dialogs

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FilterBottonDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class FilterBottomSheetDialog : BottomSheetDialogFragment() {
    private var _binding: FilterBottonDialogBinding? = null
    private val binding get() = _binding!!
    // Estado de checkboxes
    var selectedLoanTypes = mutableListOf<String>()
    var selectedLoanStatus = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FilterBottonDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
        val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

        val buttonFiltro1 = view.findViewById<MaterialButton>(R.id.buttonFiltro1)
        val optionsFiltro1 = view.findViewById<LinearLayout>(R.id.optionsFiltro1)
        optionsFiltro1.visibility = View.GONE  // Ocultar

        val buttonFiltro2 = view.findViewById<MaterialButton>(R.id.buttonFiltro2)
        val optionsFiltro2 = view.findViewById<LinearLayout>(R.id.optionsFiltro2)
        optionsFiltro2.visibility = View.GONE  // Ocultar

        val buttonClearFilters = view.findViewById<MaterialButton>(R.id.buttonClearFilters)
        val buttonApplyFilters = view.findViewById<MaterialButton>(R.id.buttonApplyFilters)

        // Cargar opciones de filtros
        loadSelectedFilters()

        // Mostrar/Ocultar opciones dentro del contenedor con borde
        buttonFiltro1.setOnClickListener {

            optionsFiltro1.visibility = if (optionsFiltro1.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            Log.d("FilterBottomSheetDialog", "Estado inicial: ${selectedLoanTypes}")

            // Restore checked states after making visible
            if (optionsFiltro1.visibility == View.VISIBLE) {
                updateCheckboxTypes(optionsFiltro1, view, checkedColor, uncheckedColor)
            }
        }

        buttonFiltro2.setOnClickListener {

            optionsFiltro2.visibility = if (optionsFiltro2.visibility == View.VISIBLE) View.GONE else View.VISIBLE

            // Restore checked states after making visible
            if (optionsFiltro2.visibility == View.VISIBLE) {
                updateCheckboxStatus(optionsFiltro2, view, checkedColor, uncheckedColor)
            }
        }

        buttonClearFilters.setOnClickListener {
            clearAllCheckBoxes()
        }

        buttonApplyFilters.setOnClickListener {
            dismiss()
            // Guardar filtros seleccionados
            saveSelectedFilters()

            // Retornar datos para filtrar
            val result = Bundle().apply {
                putStringArrayList("typeFilters", ArrayList(selectedLoanTypes))
                putStringArrayList("statusFilters", ArrayList(selectedLoanStatus))
            }
            parentFragmentManager.setFragmentResult("filterRequestKey", result)
        }

/*        val checkBoxOpOne = binding.checkFiltro1Op1
        val checkBoxOptwo = binding.checkFiltro1Op2
        val checkBoxOptree = binding.checkFiltro1Op3
        val checkBoxOp2one = binding.checkFiltro2Op1
        val checkBoxOp2two = binding.checkFiltro2Op2
        val checkBoxOp2tree = binding.checkFiltro2Op3
        val checkedColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
        val uncheckedColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_600)

        checkBoxOpOne.setOnCheckedChangeListener { _, isChecked ->
            checkBoxOpOne.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
        }
        checkBoxOpOne.setOnCheckedChangeListener { _, isChecked ->
            checkBoxOptwo.buttonTintList = ColorStateList.valueOf(if (isChecked) checkedColor else uncheckedColor)
        }*/

    }



    // Filtro de tipos
    private fun updateSelectedTypesFilters(view: View) {
        val optionsFiltro1 = view.findViewById<LinearLayout>(R.id.optionsFiltro1)

        for (i in 0 until optionsFiltro1.childCount) {
            val child = optionsFiltro1.getChildAt(i)
            if (child is CheckBox && child.isChecked && !selectedLoanTypes.contains(child.text.toString())) {
                selectedLoanTypes.add(child.text.toString())
                /*if( child.text.toString() == "Préstamo Largo Plazo (PLP)")
                    selectedLoanTypes.add("Préstamo Largo Plazo")
                else selectedLoanTypes.add(child.text.toString())*/
            }
            else if (child is CheckBox && !child.isChecked && selectedLoanTypes.contains(child.text.toString())) {
                selectedLoanTypes.remove(child.text.toString())
                /*if( child.text.toString() == "Préstamo Largo Plazo (PLP)")
                    selectedLoanTypes.remove("Préstamo Largo Plazo")
                else selectedLoanTypes.remove(child.text.toString())*/
            }
        }
        //Log.d("FilterBottomSheetDialog", "Currently selected: ${selectedLoanTypes.joinToString(", ")}")
    }

    // Filtro de estados
    private fun updateSelectedStatusFilters(view: View) {
        val optionsFiltro2 = view.findViewById<LinearLayout>(R.id.optionsFiltro2)

        for (i in 0 until optionsFiltro2.childCount) {
            val child = optionsFiltro2.getChildAt(i)
            if (child is CheckBox && child.isChecked && !selectedLoanStatus.contains(child.text.toString())) {
                selectedLoanStatus.add(child.text.toString())
            }
            else if (child is CheckBox && !child.isChecked && selectedLoanStatus.contains(child.text.toString())) {
                selectedLoanStatus.remove(child.text.toString())
            }
        }
        //Log.d("FilterBottomSheetDialog", "Currently selected: ${selectedLoanTypes.joinToString(", ")}")
    }

    private fun clearAllCheckBoxes() {
        val containerFiltro1 = view?.findViewById<LinearLayout>(R.id.optionsFiltro1)
        val containerFiltro2 = view?.findViewById<LinearLayout>(R.id.optionsFiltro2)

        val containers = listOf(containerFiltro1, containerFiltro2)

        for (container in containers) {
            if (container != null) {
                for (i in 0 until container.childCount) {
                    val child = container.getChildAt(i)
                    if (child is CheckBox) {
                        child.isChecked = false
                    }
                }
            }
        }
        // Limpiar listas
        selectedLoanTypes = mutableListOf<String>()
        selectedLoanStatus = mutableListOf<String>()
    }

    // Guarsdar / cargar opciones de filtros
    private fun saveSelectedFilters() {
        val sharedPref = requireContext().getSharedPreferences("FilterPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet("selectedLoanTypes", selectedLoanTypes.toSet()) // Convertimos la lista en un Set<String>
            putStringSet("selectedLoanStatus", selectedLoanStatus.toSet())
            apply()
        }
    }

    private fun loadSelectedFilters() {
        val sharedPref = requireContext().getSharedPreferences("FilterPrefs", Context.MODE_PRIVATE)
        val savedFilters1 = sharedPref.getStringSet("selectedLoanTypes", emptySet()) ?: emptySet()
        val savedFilters2 = sharedPref.getStringSet("selectedLoanStatus", emptySet()) ?: emptySet()
        selectedLoanTypes = savedFilters1.toMutableList()
        selectedLoanStatus = savedFilters2.toMutableList()
    }

    // Actualizar estqado de opciones de filtros
    private fun updateCheckboxTypes(element: LinearLayout, view: View,  checkedColor: Int, uncheckedColor: Int) {
        for (i in 0 until element.childCount) {
            val child = element.getChildAt(i)
            if (child is CheckBox) {
                child.isChecked = selectedLoanTypes.contains(child.text.toString()) // Marcar si está guardado

                child.buttonTintList = ColorStateList.valueOf(
                    if (child.isChecked) checkedColor else uncheckedColor
                )
                /*if( child.text.toString() == "Préstamo Largo Plazo (PLP)")
                    child.isChecked = selectedLoanTypes.contains("Préstamo Largo Plazo")
                else child.isChecked = selectedLoanTypes.contains(child.text.toString())*/

                child.setOnCheckedChangeListener { _, isChecked ->
                    //Log.d("Checkbox Status", "Checkbox '${child.text}' isChecked: $isChecked")
                    child.buttonTintList = ColorStateList.valueOf(
                        if (isChecked) checkedColor else uncheckedColor
                    )
                    updateSelectedTypesFilters(view)
                }
            }
        }
    }

    private fun updateCheckboxStatus(element: LinearLayout, view: View, checkedColor: Int, uncheckedColor: Int) {
        for (i in 0 until element.childCount) {
            val child = element.getChildAt(i)
            if (child is CheckBox) {
                child.isChecked = selectedLoanStatus.contains(child.text.toString()) // Marcar si está guardado
                child.buttonTintList = ColorStateList.valueOf(
                    if (child.isChecked) checkedColor else uncheckedColor
                )
                // Filtro "En revisión"
                if( child.text.toString() == "En Revisión")
                    child.isChecked = selectedLoanStatus.contains("En revisión")
                else child.isChecked = selectedLoanStatus.contains(child.text.toString())

                child.setOnCheckedChangeListener { _, isChecked ->
                    Log.d("Checkbox Status", "Checkbox '${child.text}' isChecked: $isChecked")
                    child.buttonTintList = ColorStateList.valueOf(
                        if (isChecked) checkedColor else uncheckedColor
                    )
                    updateSelectedStatusFilters(view)
                }
            }
        }
    }
}