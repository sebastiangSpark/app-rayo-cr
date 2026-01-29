package com.rayo.rayoxml.CO.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rayo.rayoxml.databinding.FragmentProfileAddressBinding

class ProfileAddressFragment : Fragment() {

    private var _binding: FragmentProfileAddressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileAddressBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

}