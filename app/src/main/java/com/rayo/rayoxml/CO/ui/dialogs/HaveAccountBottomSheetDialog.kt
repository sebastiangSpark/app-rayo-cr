package com.rayo.rayoxml.co.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rayo.rayoxml.databinding.HaveAccountBottonSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HaveAccountBottomSheetDialog(private val listener: (String) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: HaveAccountBottonSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HaveAccountBottonSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.itemSi.setOnClickListener {
            listener("Sí")
            dismiss()
        }
        /*binding.radioSi.setOnClickListener {
            listener("Sí")
            dismiss()
        }*/

        binding.itemNo.setOnClickListener {
            listener("No")
            dismiss()
        }
        /*binding.radioNo.setOnClickListener {
            listener("No")
            dismiss()
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}