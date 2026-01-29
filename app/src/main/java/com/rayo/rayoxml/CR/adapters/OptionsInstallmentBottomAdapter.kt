package com.rayo.rayoxml.cr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R

class OptionsInstallmentBottomAdapter(
    private val items: List<OptionWithIcon>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<OptionsInstallmentBottomAdapter.OptionViewHolder>() {

    // Clase de datos que ahora incluye texto e icono
    data class OptionWithIcon(val text: String, val iconResId: Int, val disableOption: Boolean = false)

    class OptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val optionText: TextView = view.findViewById(R.id.option_text)
        private val optionIcon: ImageView = view.findViewById(R.id.option_icon)

        fun bind(option: OptionWithIcon, onItemClick: (String) -> Unit) {
            optionText.text = option.text
            optionIcon.setImageResource(option.iconResId)

            if(option.disableOption){
                // Cambiar estilo a deshabilitado
                val context = itemView.context
                val textColor = ContextCompat.getColor(context, R.color.woodsmoke_300)
                optionIcon.setColorFilter(textColor)
                optionText.setTextColor(textColor)
                // Deshabiltar botón
                itemView.isEnabled = false
                itemView.isClickable = false
            }else{
                itemView.isEnabled = true
                itemView.isClickable = true
                itemView.setOnClickListener { onItemClick(option.text) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option_installment_with_icon, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount() = items.size
}