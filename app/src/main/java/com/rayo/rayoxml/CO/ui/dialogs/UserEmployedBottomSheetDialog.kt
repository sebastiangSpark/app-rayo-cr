package com.rayo.rayoxml.co.ui.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.UserEmployedBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserEmployedBottomSheetDialog(private val listener: (String) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: UserEmployedBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserEmployedBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.Matisse_700))
        binding.radioSi.buttonTintList = colorStateList
        binding.radioNo.buttonTintList = colorStateList

        binding.itemSi.setOnClickListener {
            binding.radioSi.isChecked = true
            binding.radioNo.isChecked = false
            listener("Sí")
            dismiss()
        }

        binding.itemNo.setOnClickListener {
            binding.radioNo.isChecked = true
            binding.radioSi.isChecked = false
            listener("No")
            dismiss()
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_si -> {
                    listener("Sí")
                    dismiss()
                }
                R.id.radio_no -> {
                    listener("No")
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}