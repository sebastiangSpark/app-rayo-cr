package com.rayo.rayoxml.co.adapters

import android.graphics.drawable.GradientDrawable
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.databinding.FragmentPrincipalCardLoanBinding
import com.rayo.rayoxml.co.models.PrincipalLoanCardData
import java.util.Locale

class PrincipalCardLoanAdapter(
    private val onButtonOpenBottomSheetClick: (PrincipalLoanCardData) -> Unit, // Lambda para el primer botón
    private val onButtonOpenPaymentInstallmentClick: (PrincipalLoanCardData) -> Unit // Lambda para el segundo botón
) : RecyclerView.Adapter<PrincipalCardLoanAdapter.LoanViewHolder>() {

    private val items = mutableListOf<PrincipalLoanCardData>()

    fun setItems(newItems: List<PrincipalLoanCardData>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val binding = FragmentPrincipalCardLoanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoanViewHolder(binding, onButtonOpenBottomSheetClick, onButtonOpenPaymentInstallmentClick)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class LoanViewHolder(private val binding: FragmentPrincipalCardLoanBinding,
                         private val onButtonOpenBottomSheetClick: (PrincipalLoanCardData) -> Unit, // Lambda para el primer botón
                         private val onButtonOpenPaymentInstallmentClick: (PrincipalLoanCardData) -> Unit // Lambda para el segundo botón
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrincipalLoanCardData) {
            binding.cardTitle.text = item.loanType
            binding.cardAmount.text = item.amount
            binding.cardState.text = item.state
            binding.cardPayment.text = item.payment
            binding.cardDate.text = formatDate(item.date) //"${item.date}"

            // Cambia colores según el estado
            val context = binding.root.context
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f // 8dp de border-radius
            }

            when (item.state) {
                "Pendiente" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.pendingBackground))
                    binding.cardState.setTextColor(ContextCompat.getColor(context, R.color.pendingText))
                }
                "Pendiente Desembolso" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.pendingBackground))
                    binding.cardState.setTextColor(ContextCompat.getColor(context, R.color.pendingText))
                    binding.buttonOpenPaymentInstallment.visibility = View.INVISIBLE
                }
                "Cancelado" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.canceledBackground))
                    binding.cardState.setTextColor(ContextCompat.getColor(context, R.color.canceledText))
                    binding.buttonOpenPaymentInstallment.visibility = View.INVISIBLE
                }
                "En mora" -> {
                    backgroundDrawable.setColor(ContextCompat.getColor(context, R.color.overdueBackground))
                    binding.cardState.setTextColor(ContextCompat.getColor(context, R.color.overdueText))
                }
            }

            binding.cardState.background = backgroundDrawable

            binding.buttonOpenBottomSheet.setOnClickListener {
                onButtonOpenBottomSheetClick(item) // Ejecutar la lambda con el ítem seleccionado
            }
            binding.buttonOpenPaymentInstallment.setOnClickListener {
                onButtonOpenPaymentInstallmentClick(item) // Ejecutar la lambda con el ítem seleccionado
            }
        }
    }

    companion object {
        fun formatDate(dateString: String): String {
            return try {
                // Extrae solo la parte de la fecha (últimos 10 caracteres "YYYY-MM-DD")
                val cleanDate = dateString.takeLast(10)

                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es", "ES")) // Ej: 30 de noviembre de 2024

                val date = inputFormat.parse(cleanDate)
                "Próximo pago: " + outputFormat.format(date ?: cleanDate).replaceFirstChar { it.uppercaseChar() }
            } catch (e: Exception) {
                dateString // Si hay error, devuelve la cadena original
            }
        }
    }
}