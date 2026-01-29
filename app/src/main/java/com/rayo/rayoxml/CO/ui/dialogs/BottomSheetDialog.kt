package com.rayo.rayoxml.co.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rayo.rayoxml.databinding.BottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private var buttonClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("title") ?: "Default Title"
        val message = arguments?.getString("message") ?: "Default Message"
        val buttonText = arguments?.getString("buttonText") ?: "OK"

        binding.titleSheetDialog.text = title
        binding.textSheetDialog.text = message
        binding.buttonSheetDialog.text = buttonText

        binding.buttonSheetDialog.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, message: String, buttonText: String): BottomSheetDialog {
            return BottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("message", message)
                    putString("buttonText", buttonText)
                }
            }
        }
    }
}