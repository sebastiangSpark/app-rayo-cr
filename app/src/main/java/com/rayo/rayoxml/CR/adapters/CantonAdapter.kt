package com.rayo.rayoxml.cr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.models.Canton
import com.rayo.rayoxml.databinding.ItemTypeAccountBinding

class CantonAdapter(
    private val items: List<Canton>,
    private val onItemClick: (Canton) -> Unit
) : RecyclerView.Adapter<CantonAdapter.CantonViewHolder>() {

    //private val items = listOf("Cartago", "Paraíso", "La Unión", "Jiménez", "Turrialba", "Oreamuno")

    inner class CantonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDepartamento: TextView = itemView.findViewById(R.id.txtDepartamento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CantonViewHolder {
        //val binding = ItemTypeAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //return ViewHolder(binding, onItemClick())
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_department, parent, false)
        return CantonViewHolder(view)
    }

    override fun onBindViewHolder(holder: CantonViewHolder, position: Int) {
        //holder.bind(items[position].nombre)
        val canton = items[position]
        holder.txtDepartamento.text = canton.nombre
        holder.itemView.setOnClickListener { onItemClick(canton) }
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