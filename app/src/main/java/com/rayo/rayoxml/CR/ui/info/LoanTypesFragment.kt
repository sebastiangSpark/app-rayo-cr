package com.rayo.rayoxml.cr.ui.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentLoanTypesBinding

class LoanTypesFragment : Fragment() {

    private var _binding: FragmentLoanTypesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanTypesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = layoutInflater.inflate(R.layout.toolbar_loan_types, binding.root as ViewGroup, false)
        (binding.root as ViewGroup).addView(toolbar, 0) // Añadir el Toolbar al inicio del layout

        // Configurar el botón de retroceso
        val backButton = toolbar.findViewById<ImageView>(R.id.back_toolbar_icon)
        backButton.setOnClickListener {
            findNavController().navigateUp() // Navegar hacia atrás
        }

        // Botones
        val button1 = view.findViewById<Button>(R.id.buttonMini)
        val button2 = view.findViewById<Button>(R.id.buttonPlus)
        val button3 = view.findViewById<Button>(R.id.buttonLarge)

        // Manejo de clics
        button1.setOnClickListener {
            findNavController().navigate(R.id.miniScreenFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.loanTypesFragment, false) // Mantén loanTypesFragment en la pila
                .build())
        }
        button2.setOnClickListener {
            findNavController().navigate(R.id.plusScreenFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.loanTypesFragment, false) // Mantén loanTypesFragment en la pila
                .build())
        }
        button3.setOnClickListener {
            findNavController().navigate(R.id.largeScreenFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.loanTypesFragment, false) // Mantén loanTypesFragment en la pila
                .build())
        }
    }

}