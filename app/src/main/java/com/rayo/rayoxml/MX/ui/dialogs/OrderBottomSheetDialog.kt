package com.rayo.rayoxml.mx.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.OrderLoanAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.order_botton_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerOrderMenu)

        val options = listOf(
            "Monto más alto",
            "Monto más bajo",
            "Con más cuotas primero",
            "Con menos cuotas primero",
            "Recientes primero",
            "Antiguos primero"
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = OrderLoanAdapter(options) { selectedOption ->
            //Toast.makeText(requireContext(), "Seleccionaste: $selectedOption", Toast.LENGTH_SHORT).show()
            // Send the selected option to PersonalLoanFragment
            val result = Bundle().apply { putString("selectedOrder", selectedOption) }
            setFragmentResult("orderRequestKey", result)
            dismiss() // Cierra el diálogo cuando se selecciona una opción
        }
    }
}