package com.rayo.rayoxml.cr.ui.loan

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
import com.rayo.rayoxml.cr.adapters.PaymentAdapter
import com.rayo.rayoxml.databinding.FragmentLoanDetailBinding
import com.rayo.rayoxml.cr.services.Auth.Prestamo
import com.rayo.rayoxml.cr.services.User.UserViewModel
import com.rayo.rayoxml.utils.ContactoPyZ
import com.rayo.rayoxml.utils.PDFGenerator
import com.rayo.rayoxml.utils.PrestamoPyZ
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

// Pantalla de detalle de préstamos
class LoanDetailFragment : Fragment() {

    private var _binding: FragmentLoanDetailBinding? = null
    private val binding get() = _binding!!
    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

    private val userViewModel: UserViewModel by activityViewModels()
    private var userName: String = ""
    private var userLastName: String = ""
    private var userDocumentId: String = ""

    private val PENDING_LOAN_STATUS = "Pendiente"
    private val COMPLETED_LOAN_STATUS = "Cancelado"
    private val REVISION_LOAN_STATUS = "En Revisión"
    private val COMPLETED_PAYMENT_STATUS = "Pagado"

    private val currencySymbol = "₡"

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
                Log.d("FormFragment", "Datos personales: ${data}")
                userName = data.nombre
                userLastName = ""
                userDocumentId = data.cedula
            }
        }

        val prestamo = arguments?.getParcelable<Prestamo>("prestamoSeleccionado")
        Log.d("LoanDetailFragment", "Préstamo seleccionado: ${prestamo}")

        // Estilo botón
        val loanState = getLoanStatus(prestamo!!)
        // Desactivar botón
        binding.btnPdf.visibility = View.GONE
        if( loanState == "Pagado" || loanState == "Cancelado"){
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
                val prestamo = PrestamoPyZ(prestamo.codigoPrestamo, prestamo.codigoPrestamo, prestamo.fechaDeposito)
                PDFGenerator.createAndDownloadPdf(requireContext(), contacto, prestamo)
            }
        }

        if (prestamo != null) {
            val cardView = view.findViewById<View>(R.id.cardLayoutDetail)

            val txtLoanAmount = cardView.findViewById<TextView>(R.id.txtLoanAmount)
            val txtLoanDate = cardView.findViewById<TextView>(R.id.txtLoanDate)
            val txtLoanStatus = cardView.findViewById<TextView>(R.id.txtLoanStatus)
            val txtLoanPayments = cardView.findViewById<TextView>(R.id.txtLoanNumberPayments)

            txtLoanAmount.text = "$currencySymbol${formatter.format(prestamo.montoPrestado.toDouble())}"
            if(loanState == "En Revisión") txtLoanDate.text = loanState
            else txtLoanDate.text = "${prestamo.fechaDeposito}"
            txtLoanStatus.text = loanState
            txtLoanPayments.text = "${prestamo.pagos.size}"

            // Cambia colores según el estado
            val context = binding.root.context
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f // 8dp de border-radius
            }

            // Estilos
            when (loanState) {
                "Pendiente" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.pendingBackground))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.pendingText))
                }
                "Pagado" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.canceledBackground))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.canceledText))
                    //binding.buttonOpenPaymentInstallment.visibility = View.INVISIBLE
                }
                "Cancelado" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.canceledBackground))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.canceledText))
                    //binding.buttonOpenPaymentInstallment.visibility = View.INVISIBLE
                }
                "En Revisión" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.Matisse_100))
                    txtLoanStatus.setTextColor(ContextCompat.getColor(context, R.color.Matisse_700))
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

    private fun getLoanStatus(loan: Prestamo): String {
        return if (loan.pagos.any { it.estado != COMPLETED_PAYMENT_STATUS }) {
            PENDING_LOAN_STATUS
        } else {
            if(loan.fechaDeposito == "" && loan.pagos.isEmpty()) REVISION_LOAN_STATUS
            else COMPLETED_LOAN_STATUS
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}