package com.rayo.rayoxml.co.ui.slider_cards

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.co.services.User.Prestamo
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class CardLoanAdapter(/*private val cardList: List<Prestamo>?*/ private var cardList: MutableList<Prestamo>,
                      private val onItemClickListener: (Prestamo) -> Unit) :
    RecyclerView.Adapter<CardLoanAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemCardTitle: TextView = itemView.findViewById(R.id.itemCardTitle)
        val itemCardAmount: TextView = itemView.findViewById(R.id.itemCardAmount)
        val itemCardState: TextView = itemView.findViewById(R.id.itemCardState)
        val itemCardPayment: TextView = itemView.findViewById(R.id.itemCardPayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        //Log.d("CardLoanAdapter", "onCreateViewHolder ejecutado")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_card_loan, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        //Log.d("CardLoanAdapter", "onBindViewHolder ejecutado para posición $position")

        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##0.00")
        val card = cardList[position]
        /*val card = cardList?.get(position)*/
        //Log.d("CardLoanAdapter", "Data: ${card}")
        holder.itemCardTitle.text = card?.tipo

        val loanValue = formatter.format(card?.montoPrestado?.toDouble())
        holder.itemCardAmount.text = "$$loanValue"

        holder.itemCardState.text = card?.estado
        holder.itemCardPayment.text = "Cantidad de cuotas: ${card?.numeroCuotas}"

        val item = cardList?.get(position)
        //Log.d("CardLoanAdapter", "Data: ${item}")

        holder.itemCardState.text = item?.estado

        val context = holder.itemView.context

        val bgColor: Int
        val textColor: Int

        when (item?.estado) {
            "Pendiente" -> {
                bgColor = ContextCompat.getColor(context, R.color.web_orange_50)
                textColor = ContextCompat.getColor(
                    context,
                    R.color.web_orange_600
                ) // Amarillo con texto negro
            }

            "Cancelado" -> {
                bgColor = ContextCompat.getColor(context, R.color.cancelado)
                textColor =
                    ContextCompat.getColor(context, R.color.calypso_800) // Verde con texto blanco
            }

            "En mora" -> {
                bgColor = ContextCompat.getColor(context, R.color.monza_200)
                textColor =
                    ContextCompat.getColor(context, R.color.monza_700) // Rojo con texto blanco
            }

            else -> {
                bgColor = ContextCompat.getColor(context, R.color.pale_sky_500)
                textColor =
                    ContextCompat.getColor(context, R.color.woodsmoke_800) // Gris con texto negro
            }
        }

        // Cambiar el fondo con bordes redondeados
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(bgColor)
        drawable.cornerRadius = 12f
        holder.itemCardState.background = drawable
        holder.itemCardState.setTextColor(textColor) // Cambia el color del texto

        // Agregar el listener al itemView (la card completa)
        holder.itemView.setOnClickListener {
            onItemClickListener(card) // Llamar al listener con el préstamo seleccionado
        }
    }

    fun updateData(newList: List<Prestamo>) {
        cardList.clear()
        cardList.addAll(newList)
        notifyDataSetChanged() // Refresh RecyclerView
    }

    override fun getItemCount(): Int {
        //Log.d("CardLoanAdapter", "getItemCount: ${cardList.size}")
        return cardList.size
    }
}