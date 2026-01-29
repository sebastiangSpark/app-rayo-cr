package com.rayo.rayoxml.co.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R

class OrderLoanAdapter(
    private val options: List<String>,
    private val onOptionClick: (String) -> Unit
) : RecyclerView.Adapter<OrderLoanAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textOption: TextView = itemView.findViewById(R.id.textOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_loan, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val option = options[position]
        holder.textOption.text = option
        holder.itemView.setOnClickListener { onOptionClick(option) }
    }
    override fun getItemCount(): Int = options.size
}