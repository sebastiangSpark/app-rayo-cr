package com.rayo.rayoxml.co.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.rayo.rayoxml.co.adapters.CalendarAdapter
import com.rayo.rayoxml.co.adapters.CustomSpinnerAdapter
import com.rayo.rayoxml.databinding.FragmentDatePickerBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class DatePickerBottomSheetDialog(private val onDateSelected: (String) -> Unit) : BottomSheetDialogFragment(){

    private var _binding: FragmentDatePickerBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatePickerBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupYearSpinner()
        setupMonthSpinner()
        setupListeners()
        updateSpinners()
        updateNextYearButton()

    }

    private fun isCurrentYearMax(): Boolean {
        val currentYear = calendar.get(Calendar.YEAR)
        val maxYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear == maxYear
    }

    private fun updateNextYearButton() {
        binding.btnNextYear.isEnabled = !isCurrentYearMax()
    }

    private fun getMonthList(): List<String> {
        return listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
    }

    private fun getYearList(): List<Int> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return (1900..currentYear).toList()
    }

    private fun setupMonthSpinner() {
        val monthList = getMonthList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, monthList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = adapter

        // Seleccionar el mes actual por defecto
        val currentMonth = calendar.get(Calendar.MONTH)
        binding.spinnerMonth.setSelection(currentMonth)

        // Listener para cuando se selecciona un mes
        binding.spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calendar.set(Calendar.MONTH, position)
                updateCalendar()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupYearSpinner() {
        val yearList = getYearList().map { it.toString() } // Convertimos la lista de Int a String
        val adapter = CustomSpinnerAdapter(requireContext(), yearList)
        binding.spinnerYear.adapter = adapter

        // Seleccionar el año actual por defecto
        val currentYear = calendar.get(Calendar.YEAR).toString()
        val defaultPosition = yearList.indexOf(currentYear)
        binding.spinnerYear.setSelection(defaultPosition)

        // Listener para cuando se selecciona un año
        binding.spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = yearList[position].toInt() // Convertimos de nuevo a Int
                calendar.set(Calendar.YEAR, selectedYear)
                updateCalendar()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter(
            onDateSelected = { date ->
                selectedDate = date
            },
            currentMonth = calendar.get(Calendar.MONTH),
            currentYear = calendar.get(Calendar.YEAR)
        )

        binding.recyclerCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.recyclerCalendar.adapter = calendarAdapter

        updateCalendar()
    }

    private fun updateSpinners() {
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Actualizar el Spinner de meses
        binding.spinnerMonth.setSelection(currentMonth)

        // Actualizar el Spinner de años
        val yearList = getYearList()
        val yearPosition = yearList.indexOf(currentYear)
        if (yearPosition != -1) {
            binding.spinnerYear.setSelection(yearPosition)
        }
    }

    private fun setupListeners() {

        // Cambiar al mes anterior
        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
            updateSpinners()
            updateNextYearButton()
        }

        // Cambiar al mes siguiente
        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
            updateSpinners()
            updateNextYearButton()
        }

        // Cambiar al año anterior
        binding.btnPrevYear.setOnClickListener {
            calendar.add(Calendar.YEAR, -1)
            updateCalendar()
            updateSpinners()
            updateNextYearButton()
        }

        // Cambiar al año siguiente
        binding.btnNextYear.setOnClickListener {
            calendar.add(Calendar.YEAR, 1)
            updateCalendar()
            updateSpinners()
            updateNextYearButton()
        }

        // Confirmar selección
        binding.btnConfirm.setOnClickListener {
            selectedDate?.let { onDateSelected(it) } ?: run {
                Toast.makeText(requireContext(), "Por favor, selecciona una fecha", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }

        // Cancelar
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun updateCalendar() {
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        // Actualiza el mes y el año en el adapter
        calendarAdapter.updateCurrentMonthYear(month, year)

        // Actualiza los días del mes
        val days = getDaysInMonth(month, year)
        calendarAdapter.updateDays(days)
    }

    private fun getDaysInMonth(month: Int, year: Int): List<String> {
        val days = mutableListOf<String>()
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(year, month, 1)

        val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..maxDay) {
            days.add(i.toString())
        }

        return days
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}