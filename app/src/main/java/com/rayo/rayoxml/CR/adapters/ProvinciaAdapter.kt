package com.rayo.rayoxml.cr.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.models.Canton
import com.rayo.rayoxml.cr.models.Provincia
import com.rayo.rayoxml.databinding.ItemTypeAccountBinding

class ProvinciaAdapter(
    private val items: List<Provincia>,
    private val onItemClick: (Provincia) -> Unit
) : RecyclerView.Adapter<ProvinciaAdapter.ProvinciaViewHolder>() {

    //private val items = listOf("Alajuela", "Cartago", "Guanacaste", "Heredia", "Limón", "Puntaneras")

    inner class ProvinciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDepartamento: TextView = itemView.findViewById(R.id.txtDepartamento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinciaViewHolder {
        //val binding = ItemTypeAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //return ViewHolder(binding, onItemClick)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_department, parent, false)
        return ProvinciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProvinciaViewHolder, position: Int) {
        Log.d("ProvinciaAdapter", "Provincia: ${items[position]}")
        //holder.bind(items[position].provincia)
        val provincia = items[position]
        holder.txtDepartamento.text = provincia.provincia
        holder.itemView.setOnClickListener { onItemClick(provincia) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemTypeAccountBinding, private val onItemClick: (String) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String) {
            binding.txtTypeAccount.text = text
            binding.root.setOnClickListener { onItemClick(text) }
        }
    }
}