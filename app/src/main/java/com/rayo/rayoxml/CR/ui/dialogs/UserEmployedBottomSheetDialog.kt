package com.rayo.rayoxml.cr.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.itemSi.setOnClickListener {
            listener("Sí")
            dismiss()
        }

        binding.itemNo.setOnClickListener {
            listener("No")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}