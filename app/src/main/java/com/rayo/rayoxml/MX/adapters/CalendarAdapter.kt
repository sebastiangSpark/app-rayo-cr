package com.rayo.rayoxml.mx.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R

class CalendarAdapter(private val onDateSelected: (String) -> Unit, // Callback para la fecha seleccionada
                      private var currentMonth: Int, // Mes actual
                      private var currentYear: Int // Año actual
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = mutableListOf<String>()
    private var selectedPosition = -1

    // Método para actualizar el mes y el año actual
    fun updateCurrentMonthYear(month: Int, year: Int) {
        currentMonth = month
        currentYear = year
        selectedPosition = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, position)
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<String>) {
        days.clear()
        days.addAll(newDays)
        notifyDataSetChanged()
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDay: TextView = itemView.findViewById(R.id.txtDay)

        fun bind(day: String, position: Int) {
            txtDay.text = day

            if (selectedPosition == position) {
                itemView.setBackgroundResource(R.drawable.selected_day_bg)
                txtDay.setTextColor(Color.WHITE)
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
                txtDay.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)

                val selectedDate = "$day/${currentMonth + 1}/$currentYear"
                onDateSelected(selectedDate)
            }
        }
    }
}