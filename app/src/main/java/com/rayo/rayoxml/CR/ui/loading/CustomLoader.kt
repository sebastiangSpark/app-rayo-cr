package com.rayo.rayoxml.cr.ui.loading

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.rayo.rayoxml.R

class CustomLoader@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val bars: List<ImageView>
    private var currentIndex = 0 // Para controlar qué barra está animándose
    private var previousIndex = -1 // Para controlar la barra previamente animada

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_loader, this, true)
        bars = listOf(
            findViewById(R.id.bar1),
            findViewById(R.id.bar2),
            findViewById(R.id.bar3),
            findViewById(R.id.bar4),
            findViewById(R.id.bar5),
            findViewById(R.id.bar6)
        )

        startAnimation()
    }

    private fun startAnimation() {
        val animator = ValueAnimator.ofInt(0, bars.size - 1).apply {
            duration = 1200 // Tiempo total de la animación de una ola completa
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                currentIndex = animation.animatedValue as Int
                updateBars(currentIndex)
            }
        }
        animator.start()
    }

    private fun updateBars(index: Int) {
        if (previousIndex != -1) {
            // Restablecer la barra anterior a su tamaño original
            bars[previousIndex].scaleX = 1f
            bars[previousIndex].scaleY = 1f
            bars[previousIndex].setImageResource(R.drawable.bar_loader_small)
        }

        // Escalar la barra actual
        bars[index].scaleX = 1.7f // Ajusta esto para cambiar la escala horizontal
        bars[index].scaleY = 1.7f // Ajusta esto para cambiar la escala vertical
        bars[index].setImageResource(R.drawable.bar_loader_large)

        previousIndex = index
    }
}