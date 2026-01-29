package com.rayo.rayoxml.CR.ui.loan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rayo.rayoxml.databinding.FragmentCrLoanBinding


class CrLoanFragment : Fragment() {

    private var _binding: FragmentCrLoanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrLoanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


}