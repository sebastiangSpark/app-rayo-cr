package com.rayo.rayoxml.co.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentPayProcessResponseBinding
import com.rayo.rayoxml.co.models.DialogState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PayProcessResponse : BottomSheetDialogFragment() {

    private var _binding: FragmentPayProcessResponseBinding? = null
    private val binding get() = _binding!!

    private var dialogState: DialogState? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayProcessResponseBinding.inflate(inflater, container, false)
        val view = binding.root

/*        FORMA de USARLO:
            val successDialog = PayProcessResponse.newInstance(DialogState.SUCCESS) SUCCESS, PROCESSING, FAILED //aqui cambias el estado
            successDialog.show(parentFragmentManager, "PayProcessResponse")*/

        when (dialogState) {
            DialogState.SUCCESS -> {
                binding.dialogTitle.text = "¡Pago realizado con éxito!"
                binding.dialogMessage.text = "Tu pago ha sido procesado correctamente. Puedes revisar más información en los detalles de crédito"
                binding.dialogImage.setImageResource(R.drawable.status_icon_done)
            }
            DialogState.PROCESSING -> {
                binding.dialogTitle.text = "Procesando tu pago"
                binding.dialogMessage.text = "Estamos verificando la transacción. Este proceso puede tardar unos minutos."
                binding.dialogImage.setImageResource(R.drawable.status_icon_clock)
            }
            DialogState.FAILED -> {
                binding.dialogTitle.text = "No pudimos procesar tu pago"
                binding.dialogMessage.text = "Ocurrió un problema al procesar tu transacción. Por favor, verifica tus datos e inténtalo nuevamente."
                binding.dialogImage.setImageResource(R.drawable.status_icon_faile)
            }
            null -> {

            }
        }

        binding.dialogButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    companion object {
        fun newInstance(state: DialogState): PayProcessResponse {
            val fragment = PayProcessResponse()
            fragment.dialogState = state
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}