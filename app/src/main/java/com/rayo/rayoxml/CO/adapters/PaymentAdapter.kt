package com.rayo.rayoxml.co.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.services.User.Pago
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class PaymentAdapter(private val paymentList: MutableList<Pago>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val installmentAmount: TextView = itemView.findViewById(R.id.installmentAmount)
        val installmentDueDate: TextView = itemView.findViewById(R.id.installmentDueDate)
        val installmentStatus: TextView = itemView.findViewById(R.id.installmentStatus)
        val installmentCode: TextView = itemView.findViewById(R.id.installmentCode)
        val installmentStatusImage: ImageView= itemView.findViewById(R.id.installmentStatusImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        formatter.applyPattern("#,##0.00")  // Formato de moneda
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_installment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentList[position]

        holder.installmentAmount.text = "$${formatter.format(payment.totalPagarGAC.toDouble())}"
        holder.installmentDueDate.text = "${payment.fechaPago}"
        holder.installmentStatus.text = "${payment.estado}"
        holder.installmentCode.text = "${payment.codigo}"

        // Cambiar color del estado según el pago
        val context = holder.itemView.context
        when (payment.estado) {
            "Pagado" -> {
                holder.installmentStatusImage.setImageResource(R.drawable.ic_canceled_detail) // Imagen para Cancelado
            }
            "Pendiente" -> {
                holder.installmentStatusImage.setImageResource(R.drawable.ic_pending_detail) // Imagen para Pendiente
            }
            "En mora" -> {
                holder.installmentStatusImage.setImageResource(R.drawable.ic_mora_detail) // Imagen para En mora
            }
            else -> {
                holder.installmentStatusImage.setImageResource(R.drawable.ic_pending_detail) // Imagen por defecto
            }
        }
    }

    override fun getItemCount(): Int = paymentList.size
}