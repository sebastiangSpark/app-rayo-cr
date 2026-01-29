package com.rayo.rayoxml.cr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.models.PaymentDate
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class LoanPaymentAdapter(private val items: MutableList<PaymentDate>) :
    RecyclerView.Adapter<LoanPaymentAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
    private val currencySymbol = "₡"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.payment_date)
        val value: TextView = view.findViewById(R.id.payment_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        formatter.applyPattern("#,##0.00")  // Ensures two decimal places
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_credit_payment_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, value) = items[position]
        holder.date.text = name
        holder.value.text = "$currencySymbol${formatter.format(value)}"
    }

    override fun getItemCount() = items.size

    fun updateData(newList: List<PaymentDate>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged() // Refresh RecyclerView
    }
}