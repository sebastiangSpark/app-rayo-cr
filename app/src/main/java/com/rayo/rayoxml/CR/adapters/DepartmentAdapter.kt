package com.rayo.rayoxml.cr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R

class DepartmentAdapter(
    private val departamentos: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DepartmentAdapter.DepartamentoViewHolder>() {

    inner class DepartamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDepartamento: TextView = itemView.findViewById(R.id.txtDepartamento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartamentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_department, parent, false)
        return DepartamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartamentoViewHolder, position: Int) {
        val departamento = departamentos[position]
        holder.txtDepartamento.text = departamento
        holder.itemView.setOnClickListener { onItemClick(departamento) }
    }

    override fun getItemCount(): Int = departamentos.size
}