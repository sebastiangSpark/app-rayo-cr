package com.rayo.rayoxml.CO.ui.info

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
import com.rayo.rayoxml.databinding.FragmentBenefitBinding

class BenefitFragment : Fragment() {

    private var _binding: FragmentBenefitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBenefitBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = layoutInflater.inflate(R.layout.toolbar_benefit, binding.root as ViewGroup, false)
        (binding.root as ViewGroup).addView(toolbar, 0) // Añadir el Toolbar al inicio del layout

        // Configurar el botón de retroceso
        val backButton = toolbar.findViewById<ImageView>(R.id.back_toolbar_icon)
        backButton.setOnClickListener {
            findNavController().navigateUp() // Navegar hacia atrás
        }

        // Botones
        val button1 = view.findViewById<Button>(R.id.buttonFriend)
        val button2 = view.findViewById<Button>(R.id.buttonGoodFriend)
        val button3 = view.findViewById<Button>(R.id.buttonBestFriend)
        val button4 = view.findViewById<Button>(R.id.buttonSuperFriend)

        // Manejo de clics
        button1.setOnClickListener {
            findNavController().navigate(
                R.id.friendFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.benefitFragment, false)
                .build())
        }
        button2.setOnClickListener {
            findNavController().navigate(
                R.id.goodFriendFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.benefitFragment, false)
                .build())
        }
        button3.setOnClickListener {
            findNavController().navigate(
                R.id.bestFriendFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.benefitFragment, false)
                .build())
        }
        button4.setOnClickListener {
            findNavController().navigate(
                R.id.superFriendFragment, null, NavOptions.Builder()
                    .setPopUpTo(R.id.benefitFragment, false)
                    .build())
        }
    }

}