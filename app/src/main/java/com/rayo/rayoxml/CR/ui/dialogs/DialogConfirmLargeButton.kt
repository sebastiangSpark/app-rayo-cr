package com.rayo.rayoxml.CR.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentDialogConfirmLargeButtonBinding


class DialogConfirmLargeButton : DialogFragment() {
    private var _binding: FragmentDialogConfirmLargeButtonBinding? = null
    private val binding get() = _binding!!

    private var listener: DialogButtonClickListener? = null

    interface DialogButtonClickListener {
        fun onConfirmClicked()
        fun onCancelClicked()
    }

    fun setDialogButtonClickListener(listener: DialogButtonClickListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDialogConfirmLargeButtonBinding.inflate(inflater, container, false)

        val title = arguments?.getString("title") ?: "Información no disponible"
        val message = arguments?.getString("message") ?: "Información no disponible"
        val confirmText = arguments?.getString("confirmText") ?: "Confirmar"
        val cancelText = arguments?.getString("cancelText") ?: "Cancelar"

        val titleMessage: TextView = binding.titleDialogLargeButton
        val textMessage: TextView = binding.textDialogLargeButton
        val confirmButton: TextView = binding.btnConfirmation
        val cancelButton: TextView = binding.btnCancel

        titleMessage.text = title
        textMessage.text = message
        confirmButton.text = confirmText
        cancelButton.text = cancelText

        confirmButton.setOnClickListener {
            listener?.onConfirmClicked()
            dismiss()
        }

        cancelButton.setOnClickListener {
            listener?.onCancelClicked()
            dismiss()
        }
        return binding.root
    }

    companion object {
        fun newInstance(
            title: String,
            message: String,
            confirmText: String,
            cancelText: String
        ): DialogConfirmLargeButton {
            val fragment = DialogConfirmLargeButton()
            val args = Bundle()
            args.putString("title", title)
            args.putString("message", message)
            args.putString("confirmText", confirmText)
            args.putString("cancelText", cancelText)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}