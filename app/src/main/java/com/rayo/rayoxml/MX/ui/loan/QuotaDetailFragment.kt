package com.rayo.rayoxml.mx.ui.loan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rayo.rayoxml.databinding.FragmentQuotaDetailBinding

class QuotaDetailFragment : Fragment() {

    private var _binding: FragmentQuotaDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuotaDetailBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Acceder a las vistas usando binding
        val cardView = binding.quotaDetailCard // Asegúrate de que el ID en el XML sea quota_detail_card
        val imageView = binding.quotaDetailCardImage // Asegúrate de que el ID en el XML sea quota_detail_card_image

        // Ajustar el margen superior de la ImageView
        val layoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = -60 // Margen negativo en píxeles
        imageView.layoutParams = layoutParams

        // Ajustar el padding superior de la CardView
        cardView.setPadding(0, 60, 0, 0)
    }
}