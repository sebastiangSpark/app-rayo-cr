package com.rayo.rayoxml.cr.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.databinding.ItemTypeAccountBinding

class GenderAdapter(private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<GenderAdapter.ViewHolder>() {

    private val items = listOf("Masculino", "Femenino")

    override fun onCreateViewHolder(parent:ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTypeAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
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