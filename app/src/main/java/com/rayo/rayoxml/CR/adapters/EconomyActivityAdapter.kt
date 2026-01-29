package com.rayo.rayoxml.cr.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.cr.models.ActividadEconomica
import com.rayo.rayoxml.databinding.ItemTypeAccountBinding

class EconomyActivityAdapter(
    private val items: List<ActividadEconomica>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<EconomyActivityAdapter.ViewHolder>() {

    //private val items = listOf("Agricultura, servicios agrícolas, caza, silvicultura y pesca", "Minas, petróleo y canteras", "Industrias manufactureras", "Electricidad, gas y agua", "Construcción", "Comercio por mayor", "Comercio por menor", "Restaurantes, cafés y otros establecimientos que expenden comidas y bebidas", "Transporte, almacenamiento y comunicaciones", "Finanzas, seguros, bienes inmuebles y servicios técnicos, profesionales y otros")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTypeAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position].name)
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