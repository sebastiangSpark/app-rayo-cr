package com.rayo.rayoxml.cr.ui.loading

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.databinding.FragmentVerificationDialogBinding
import com.rayo.rayoxml.cr.ui.loan.FormFragment

class VerificationDialogFragment : DialogFragment() {
    private var _binding: FragmentVerificationDialogBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueVerificationDialog.setOnClickListener {
            dismiss()
            (requireParentFragment() as? FormFragment)?.navigateToUserVerifyFragment()
        }
    }

}