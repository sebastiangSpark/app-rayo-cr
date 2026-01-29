package com.rayo.rayoxml.CO.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.R

class FinanceAdapter(private val items: List<FinanceItem>) :
    RecyclerView.Adapter<FinanceAdapter.FinanceViewHolder>() {

    class FinanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.title_financial_text_view) // Asegúrate de tener este ID en tu item_finance_card.xml
        private val descriptionTextView: TextView = view.findViewById(R.id.description_financial_text_view) // Asegúrate de tener este ID
        private val imageView: ImageView = view.findViewById(R.id.card_financial_image_view) // Asegúrate de tener este ID

        fun bind(item: FinanceItem) {
            titleTextView.text = item.title
            descriptionTextView.text = item.description
            imageView.setImageResource(item.imageRes)

            itemView.setOnClickListener {  openUrlInBrowser(itemView.context, item.url)  }
        }

        fun openUrlInBrowser(context: Context, url: String){
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.d("FinanceAdapter", "No se pudo abrir el navegador")
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_finance_card, parent, false)
        return FinanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinanceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

// Modelo de datos para las cards
data class FinanceItem(
    val title: String,
    val description: String,
    val imageRes: Int,
    val url: String
)

// Función para crear los datos de ejemplo
fun createFinanceItems(): List<FinanceItem> = listOf(
    FinanceItem(
        "¿Qué son Mini Préstamos a empleados?",
        "Como empleado, en ciertas ocasiones el salario no alcanza a llegar a fin de mes...",
        R.drawable.loan_employed_pic,
        "https://rayo.com.co/blog/que-son-mini-prestamos-a-empleados"
    ),
    // Agrega más items aquí
    FinanceItem(
        "¿Buscando un Mini Préstamo fácil?",
        "Solicita tu Mini Préstamo en tan solo 15 minutos, sin filas, sin trámites, y todo completamente por internet. Rayo es tu mejor opción para tus trámites...",
        R.drawable.loan_pic_two,
        "https://rayo.com.co/blog/buscando-un-mini-prestamo-facil"
    ),
    FinanceItem(
        "¿Qué son los Mini Préstamos?",
    "Solicita tu Mini Préstamo en tan solo 15 minutos, sin filas, sin trámites, y todo completamente por internet. Rayo es tu mejor opción para tus trámites...",
    R.drawable.loan_pic_tree,
        "https://rayo.com.co/blog/que-son-los-mini-prestamos"
)
)