package com.rayo.rayoxml.co.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R

class BankAdapter(
    private val banks: List<String>,
    private val onBankSelected: (String) -> Unit
    ) : RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    class BankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtBank: TextView = view.findViewById(R.id.txtBank)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank, parent, false)
        return BankViewHolder(view)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bancos = banks[position]
        holder.txtBank.text = bancos

        holder.itemView.setOnClickListener {
            onBankSelected(bancos)
        }
    }

    override fun getItemCount(): Int = banks.size
}