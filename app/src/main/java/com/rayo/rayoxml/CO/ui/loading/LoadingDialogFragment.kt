package com.rayo.rayoxml.co.ui.loading

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.rayo.rayoxml.databinding.FragmentLoadingDialogBinding

class LoadingDialogFragment(private val dismissTime: Int = 2000) :  DialogFragment() {

    private var _binding: FragmentLoadingDialogBinding? = null
    private val binding get() = _binding!!
    private var showText: Boolean = false

    companion object {
        private const val ARG_SHOW_TEXT = "show_text"

        fun newInstance(showText: Boolean = false): LoadingDialogFragment {
            val args = Bundle().apply {
                putBoolean(ARG_SHOW_TEXT, showText)
            }
            return LoadingDialogFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Leer el argumento del bundle
        showText = arguments?.getBoolean(ARG_SHOW_TEXT, false) ?: false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = FragmentLoadingDialogBinding.inflate(layoutInflater)

        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(binding.root)

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        // Configurar visibilidad del texto según el parámetro
        binding.messagesContainer.visibility = if (showText) View.VISIBLE else View.GONE
        /*Handler(Looper.getMainLooper()).postDelayed({
            dismiss()
        }, dismissTime)  // Usa el tiempo de espera recibido como parámetro*/

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}