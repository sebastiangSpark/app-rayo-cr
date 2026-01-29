package com.rayo.rayoxml.cr.ui.slider_cards

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R
import com.rayo.rayoxml.cr.services.Auth.Prestamo
//import com.rayo.rayoxml.cr.services.User.Prestamo
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

// Pantalla de listado de préstamos
class CardLoanAdapter(/*private val cardList: List<Prestamo>?*/ private var cardList: MutableList<Prestamo>,
                      private val onItemClickListener: (Prestamo) -> Unit) :
    RecyclerView.Adapter<CardLoanAdapter.CardViewHolder>() {

    private val PENDING_LOAN_STATUS = "Pendiente"
    private val COMPLETED_LOAN_STATUS = "Cancelado"
    private val REVISION_LOAN_STATUS = "En Revisión"
    private val COMPLETED_PAYMENT_STATUS = "Pagado"

    private val currencySymbol = "₡"

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
        holder.itemCardTitle.text = card.tipo

        val loanValue = formatter.format(card.montoPrestado.toDouble())
        holder.itemCardAmount.text = "$currencySymbol$loanValue"

        val loanState = getLoanStatus(card)
        //Log.d("CardLoanAdapter", "Loan state: $loanState")

        holder.itemCardState.text = loanState
        holder.itemCardPayment.text = "Cantidad de cuotas: ${card?.pagos?.size}"

        val item = cardList.get(position)
        //Log.d("CardLoanAdapter", "Data: ${item}")

        holder.itemCardState.text = loanState

        val context = holder.itemView.context

        val bgColor: Int
        val textColor: Int

        when (loanState) {
            "Pendiente" -> {
                bgColor = ContextCompat.getColor(context, R.color.web_orange_50)
                textColor = ContextCompat.getColor(
                    context,
                    R.color.web_orange_600
                ) // Amarillo con texto negro
            }

            "En Revisión" -> {
                bgColor = ContextCompat.getColor(context, R.color.Matisse_100)
                textColor = ContextCompat.getColor(
                    context,
                    R.color.Matisse_700
                )
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

    private fun getLoanStatus(loan: Prestamo): String {
        return if (loan.pagos.any { it.estado != COMPLETED_PAYMENT_STATUS }) {
            PENDING_LOAN_STATUS
        } else {
            if(loan.fechaDeposito == "" && loan.pagos.isEmpty()) REVISION_LOAN_STATUS
            else COMPLETED_LOAN_STATUS
        }
    }
}