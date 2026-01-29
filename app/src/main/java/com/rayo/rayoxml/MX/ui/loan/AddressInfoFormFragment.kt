package com.rayo.rayoxml.mx.ui.loan

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.mx.adapters.ViaAdapter
import com.rayo.rayoxml.mx.adapters.ViaAlterAdapter
import com.rayo.rayoxml.databinding.FragmentAddressInfoFormBinding
import com.rayo.rayoxml.mx.viewModels.FormViewModel

class AddressInfoFormFragment : Fragment() {

    private var _binding: FragmentAddressInfoFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FormViewModel
    private lateinit var editTextVia: EditText
    private lateinit var editTextViaAlterna: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddressInfoFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

        editTextVia = view.findViewById(R.id.form_address_via)
        editTextViaAlterna = view.findViewById(R.id.form_address_carrer)

        editTextVia.setOnClickListener {
            showBottomSheetDialogVia()
        }

        editTextViaAlterna.setOnClickListener {
            showBottomSheetDialogViaAlter()
        }

        binding.btnNextStep.setOnClickListener {
            val via = binding.formAddressVia.text.toString()
            val number = binding.formAddressNumber.text.toString()
            val carrer = binding.formAddressCarrer.text.toString()
            val complement = binding.formAddressComplement.text.toString()
            val detail = binding.formAddressDetail.text.toString()

            val bundle = bundleOf(
                "goToStep2" to true,
                "via" to via,
                "number" to number,
                "carrer" to carrer,
                "complement" to complement,
                "detail" to detail
            )

            findNavController().navigate(R.id.action_addressInfoFormFragment_to_formFragment, bundle)
        }

        binding.backToolbarAddressIcon.setOnClickListener {
            val bundle = bundleOf(
                "goToStep2" to true,
            )
            findNavController().navigate(R.id.action_addressInfoFormFragment_to_formFragment, bundle)
        }

        binding.toolbarLayoutIconAddress?.setOnClickListener{
            showInfoAddressDialog()
        }
    }

    private fun showInfoAddressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_info_address, null)

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogView.findViewById<TextView>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBottomSheetDialogVia() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_via, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewVia)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ViaAdapter { selectedType ->
            binding.formAddressVia.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showBottomSheetDialogViaAlter() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_via_alter, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext()).apply {
            setContentView(dialogView)
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewViaAlter)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ViaAlterAdapter { selectedType ->
            binding.formAddressCarrer.setText(selectedType)
            dialog.dismiss()
        }

        dialog.show()
    }
}