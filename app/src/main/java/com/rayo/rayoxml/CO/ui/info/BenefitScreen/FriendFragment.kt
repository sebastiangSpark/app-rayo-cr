package com.rayo.rayoxml.CO.ui.info.BenefitScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentFriendBinding

class FriendFragment : Fragment() {
    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = layoutInflater.inflate(R.layout.toolbar_benefit, binding.root as ViewGroup, false)
        (binding.root as ViewGroup).addView(toolbar, 0) // Añadir el Toolbar al inicio del layout

        // Configurar el botón de retroceso
        val backButton = toolbar.findViewById<ImageView>(R.id.back_toolbar_icon)
        backButton.setOnClickListener {
            if (!findNavController().navigateUp()) {
                // Si no hay un Fragment anterior, regresa a la Activity o a un Fragment predeterminado
                requireActivity().onBackPressed()
            }
        }
    }

}