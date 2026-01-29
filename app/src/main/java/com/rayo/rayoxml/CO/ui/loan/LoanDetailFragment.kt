package com.rayo.rayoxml.co.ui.loan

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.adapters.PaymentAdapter
import com.rayo.rayoxml.databinding.FragmentLoanDetailBinding
import com.rayo.rayoxml.co.services.User.Prestamo
import com.rayo.rayoxml.co.services.User.UserViewModel
import com.rayo.rayoxml.utils.ContactoPyZ
import com.rayo.rayoxml.utils.PDFGenerator
import com.rayo.rayoxml.utils.PrestamoPyZ
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class LoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailBinding? = null
    private val binding get() = _binding!!
    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

    private val userViewModel: UserViewModel by activityViewModels()
    private var userName: String = ""
    private var userLastName: String = ""
    private var userDocumentId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        formatter.applyPattern("#,##0.00")  // Formato de moneda

        userViewModel.userData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                Log.d("LoanDetailFragment", "Datos personales: ${data}")
                userName = data.nombre!!
                userLastName = data.apellidos!!
                userDocumentId = data.documento!!
            }
        }

        val prestamo = arguments?.getParcelable<Prestamo>("prestamoSeleccionado")
        Log.d("LoanDetailFragment", "Préstamo seleccionado: ${prestamo}")

        /*if (prestamo != null && prestamo.estado != "Cancelado") {
            Log.d("LoanDetailFragment", "Deshabilitar botón paz y salvo")
            val textColor = ContextCompat.getColor(requireContext(), R.color.woodsmoke_300)
            binding.btnPdf.setTextColor(textColor)
        }*/

        // Estilo botón
        if(prestamo?.estado == "Cancelado"){
            val enabledColor = ContextCompat.getColor(requireContext(), R.color.Matisse_700)
            binding.btnPdf.backgroundTintList = ColorStateList.valueOf(enabledColor)

            val textColor = ContextCompat.getColor(requireContext(), R.color.pale_sky_50)
            binding.btnPdf.setTextColor(textColor)
        }

        binding.btnBackLoanPersonal.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPdf.setOnClickListener {
            if(prestamo != null){
                val contacto = ContactoPyZ(userName, userLastName, userDocumentId)
                val prestamo = PrestamoPyZ(prestamo.prestamoId, prestamo.codigo, prestamo.fechaDesembolso)
                PDFGenerator.createAndDownloadPdf(requireContext(), contacto, prestamo)
            }
        }

        if (prestamo != null) {
            val cardView = view.findViewById<View>(R.id.cardLayoutDetail)

            val txtLoanAmount = cardView.findViewById<TextView>(R.id.txtLoanAmount)
            val txtLoanDate = cardView.findViewById<TextView>(R.id.txtLoanDate)
            val txtLoanStatus = cardView.findViewById<TextView>(R.id.txtLoanStatus)
            val txtLoanPayments = cardView.findViewById<TextView>(R.id.txtLoanNumberPayments)

            txtLoanAmount.text = "$${formatter.format(prestamo.montoPrestado.toDouble())}"
            txtLoanDate.text = " ${prestamo.fechaDesembolso}"
            txtLoanStatus.text = "${prestamo.estado}"
            txtLoanPayments.text = "${prestamo.numeroCuotas}"

            // Cambia colores según el estado
            val context = binding.root.context
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f // 8dp de border-radius
            }

            // Estilos
            when (prestamo.estado) {
                "Pendiente" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.pendingBackground))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.pendingText))
                }
                "Cancelado" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.canceledBackground))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.canceledText))
                    //binding.buttonOpenPaymentInstallment.visibility = View.INVISIBLE
                }
                "En mora" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.overdueBackground))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.overdueText))
                }
            }
            txtLoanStatus.background = backgroundDrawable

            val paymentsAdapter = PaymentAdapter(prestamo.pagos.toMutableList())
            binding.installmentsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = paymentsAdapter
            }
        } else {
            Log.e("LoanDetailFragment", "No se recibió ningún préstamo")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}