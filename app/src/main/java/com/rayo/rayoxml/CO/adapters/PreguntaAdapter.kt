package com.rayo.rayoxml.co.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rayo.rayoxml.co.services.Loan.Pregunta
import com.rayo.rayoxml.R

class PreguntaAdapter(
    private val preguntas: List<Pregunta>,
    private val onOptionSelected: (preguntaId: String, respuesta: String) -> Unit
) : RecyclerView.Adapter<PreguntaAdapter.PreguntaViewHolder>() {

    //private val selectedOptions = mutableMapOf<Int, String>()
    private val selectedOptions = mutableMapOf<String, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreguntaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pregunta, parent, false)
        return PreguntaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreguntaViewHolder, position: Int) {
        val pregunta = preguntas[position]
        holder.bind(pregunta, selectedOptions[pregunta.ordenPregunta]) // convert String to Int before using it as index
    }

    override fun getItemCount(): Int = preguntas.size

    //fun getSelectedOptions(): Map<Int, String> = selectedOptions.toMap()
    fun getSelectedOptions(): Map<String, String> = selectedOptions.toMap()

    inner class PreguntaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPregunta: TextView = itemView.findViewById(R.id.tvPregunta)
        private val rgOpciones: RadioGroup = itemView.findViewById(R.id.rgOpciones)

        fun bind(pregunta: Pregunta, selectedOptionIdRespuesta: String?) { // This accepts 'selectedOption' as 'selectedOptionIdRespuesta'
            tvPregunta.text = "${adapterPosition + 1}. ${pregunta.pregunta}"
            rgOpciones.removeAllViews()

            pregunta.respuestas.forEachIndexed { index, respuesta -> // This loops through 'respuestas' instead of 'opciones'
                val radioButton = RadioButton(itemView.context).apply {
                    text = respuesta.respuesta // Access 'respuesta' from 'OpcionRespuesta'
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) {
                            topMargin = 16.dpToPx(context) // Use your preferred dp to px conversion
                        }
                    }
                    isChecked = respuesta.idRespuesta == selectedOptionIdRespuesta // Check against 'idRespuesta' instead of 'respuesta'
                }

                radioButton.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val preguntaId = adapterPosition.toString()
                        selectedOptions[adapterPosition.toString()] = respuesta.idRespuesta
                        //onOptionSelected(preguntaId, respuesta.respuesta)
                        onOptionSelected(preguntaId, respuesta.idRespuesta) // Asignar id en lugar de texto
                    }
                }

                rgOpciones.addView(radioButton)
            }
        }
    }


    /*inner class PreguntaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPregunta: TextView = itemView.findViewById(R.id.tvPregunta)
        private val rgOpciones: RadioGroup = itemView.findViewById(R.id.rgOpciones)

        fun bind(pregunta: Pregunta, selectedOption: String?) {
            tvPregunta.text = "${pregunta.id}. ${pregunta.pregunta}"
            rgOpciones.removeAllViews()

            pregunta.opciones.forEachIndexed { index, opcion ->

                val radioButton = RadioButton(itemView.context, null, 0, R.style.RadioButtonRight).apply  {
                    text = opcion
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) {
                            topMargin = resources.getDimensionPixelSize(R.dimen.margin_top_onboarding_button)
                        }
                    }
                    isChecked = opcion == selectedOption
                }

                radioButton.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedOptions[pregunta.id] = opcion
                        onOptionSelected(pregunta.id, opcion)
                    }
                }

                rgOpciones.addView(radioButton)
            }
        }
    }*/

    // Extensión para convertir dp a px
    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
}