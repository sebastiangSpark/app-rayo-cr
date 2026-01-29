package com.rayo.rayoxml.co.ui.loading

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.rayo.rayoxml.R

class CustomLoader@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val bars: List<ImageView>

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
        fun animateBars() {
            val animations = bars.mapIndexed { index, bar ->
                val scaleAnimator = ObjectAnimator.ofFloat(bar, "scaleY", 1f, 1.7f, 1f).apply {
                    duration = 800
                    interpolator = AccelerateDecelerateInterpolator()
                    startDelay = index * 100L
                }

                // Animación adicional para scaleX (hacerla más gruesa)
                val thicknessAnimator = ObjectAnimator.ofFloat(bar, "scaleX", 1f, 1.7f, 1f).apply {
                    duration = 800
                    interpolator = AccelerateDecelerateInterpolator()
                    startDelay = index * 100L
                }

                // Listener para cambiar la imagen
                scaleAnimator.addUpdateListener { animation ->
                    val scale = animation.animatedValue as Float
                    if (scale >= 1.5f) {
                        bar.setImageResource(R.drawable.bar_loader_large)
                    } else {
                        bar.setImageResource(R.drawable.bar_loader_small)
                    }
                }

                // Animamos scaleX y scaleY juntas
                AnimatorSet().apply {
                    playTogether(scaleAnimator, thicknessAnimator)
                }
            }

            val animatorSet = AnimatorSet().apply {
                playTogether(animations)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        animateBars() // Reiniciar la animación
                    }
                })
            }
            animatorSet.start()
        }
        animateBars()
    }

}