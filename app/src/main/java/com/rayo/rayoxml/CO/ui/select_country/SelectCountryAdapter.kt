package com.rayo.rayoxml.co.ui.select_country

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.models.Country

class SelectCountryAdapter(
    private val countryList: List<Country>,
    private val onItemClick: (Country) -> Unit
) : RecyclerView.Adapter<SelectCountryAdapter.ViewHolder>() {

    private var selectedPosition = -1

    fun getSelectedCountry(): Country? {
        return if (selectedPosition != -1 && countryList[selectedPosition].enabled) {
            countryList[selectedPosition]
        } else {
            null
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardViewCountry)
        val imageViewFlag: ImageView = itemView.findViewById(R.id.imageViewFlag)
        val textViewCountryName: TextView = itemView.findViewById(R.id.textViewCountryName)
        val radioButtonCountry: RadioButton = itemView.findViewById(R.id.radioButtonCountry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = countryList[position]

        holder.imageViewFlag.setImageResource(country.flagResId)
        holder.textViewCountryName.text = country.name
        holder.radioButtonCountry.isChecked = position == selectedPosition

        // Deshabilitar elementos según el estado del país
        val isEnabled = country.enabled
        holder.cardView.isEnabled = isEnabled
        holder.radioButtonCountry.isEnabled = isEnabled
        holder.cardView.alpha = if (isEnabled) 1f else 0.5f
        holder.imageViewFlag.alpha = if (isEnabled) 1f else 0.5f
        holder.textViewCountryName.alpha = if (isEnabled) 1f else 0.5f

        // Configurar listeners solo si está habilitado
        if (isEnabled) {
            holder.cardView.setOnClickListener {
                selectedPosition = holder.adapterPosition
                notifyDataSetChanged()
                onItemClick(country)
            }

            holder.radioButtonCountry.setOnClickListener {
                selectedPosition = holder.adapterPosition
                notifyDataSetChanged()
                onItemClick(country)
            }
        } else {
            holder.cardView.setOnClickListener(null)
            holder.radioButtonCountry.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = countryList.size
}