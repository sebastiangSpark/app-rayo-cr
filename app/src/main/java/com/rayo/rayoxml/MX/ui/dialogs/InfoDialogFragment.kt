package com.rayo.rayoxml.mx.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.R

class InfoDialogFragment: DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_custom_dialog, container, false)
        val title = arguments?.getString("title") ?: "Información no disponible"
        val message = arguments?.getString("message") ?: "Información no disponible"

        val titleMessage: TextView = view.findViewById(R.id.title_info_message)
        val textMessage: TextView = view.findViewById(R.id.txt_info_message)
        val closeDialog: TextView = view.findViewById(R.id.btn_close_dialog)

        titleMessage.text = title
        textMessage.text = message

        closeDialog.setOnClickListener {
            dismiss()
        }
        return view
    }

    companion object {
        fun newInstance(title: String, message: String): InfoDialogFragment {
            val fragment = InfoDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("message", message)
            fragment.arguments = args
            return fragment
        }
    }
}