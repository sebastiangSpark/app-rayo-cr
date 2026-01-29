package com.rayo.rayoxml.cr.ui.renewal.custombutton

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.rayo.rayoxml.CR.ui.renewal.custombutton.ButtonValue
import com.rayo.rayoxml.R

class CustomButtonSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    private val buttons = mutableListOf<Button>()
    private val buttonValues = mutableListOf<ButtonValue>()
    private var selectedPosition = -1
    private var onSelectionChangedListener: ((position: Int, value: String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        setPadding(24, 13, 18, 13)
    }

    // Método original que mantiene compatibilidad hacia atrás
    fun setButtonValues(values: List<String>, initialSelection: Int = 0) {
        val buttonValuesList = values.map { ButtonValue(label = it, disabled = false) }
        setButtonValuesWithState(buttonValuesList, initialSelection)
    }

    // Nuevo método que acepta ButtonValue con nombre diferente
    fun setButtonValuesWithState(values: List<ButtonValue>, initialSelection: Int = 0) {
        removeAllViews()
        buttons.clear()
        buttonValues.clear()
        buttonValues.addAll(values)

        values.forEachIndexed { index, buttonValue ->
            val button = createButton(buttonValue, index)
            buttons.add(button)
            addView(button)
        }

        if (initialSelection in 0 until buttons.size && !buttonValues[initialSelection].disabled) {
            selectButton(initialSelection)
        } else {
            // Si el botón inicial está deshabilitado, seleccionar el primer botón habilitado
            val firstEnabledIndex = buttonValues.indexOfFirst { !it.disabled }
            if (firstEnabledIndex != -1) {
                selectButton(firstEnabledIndex)
            }
        }
    }

    private fun createButton(buttonValue: ButtonValue, position: Int): Button {
        val button = Button(context).apply {
            text = buttonValue.label
            isEnabled = !buttonValue.disabled

            try {
                val typeface = ResourcesCompat.getFont(context, R.font.sf_pro_medium)
                this.typeface = typeface
            } catch (e: Exception) {
                typeface = Typeface.DEFAULT_BOLD
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                if (position > 0) leftMargin = 12
            }

            setOnClickListener {
                if (!buttonValue.disabled) {
                    selectButton(position)
                    onSelectionChangedListener?.invoke(position, buttonValue.label)
                }
            }
        }

        // Aplicar estilo inicial
        if (buttonValue.disabled) {
            applyDisabledStyle(button)
        } else {
            applyUnselectedStyle(button)
        }

        return button
    }

    private fun selectButton(position: Int) {
        if (position == selectedPosition) return
        if (position >= buttonValues.size || buttonValues[position].disabled) return

        if (selectedPosition in 0 until buttons.size) {
            val previousButtonValue = buttonValues[selectedPosition]
            if (previousButtonValue.disabled) {
                applyDisabledStyle(buttons[selectedPosition])
            } else {
                applyUnselectedStyle(buttons[selectedPosition])
            }
        }

        selectedPosition = position
        if (position in 0 until buttons.size) {
            applySelectedStyle(buttons[position])
        }
    }

    private fun applySelectedStyle(button: Button) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#FF5722"))
            cornerRadius = 90f
        }
        button.background = drawable
        button.setTextColor(Color.WHITE)
    }

    private fun applyUnselectedStyle(button: Button) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#CCCCCC"))
            cornerRadius = 90f
        }
        button.background = drawable
        button.setTextColor(Color.parseColor("#666666"))
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        button.compoundDrawablePadding = 0
    }

    private fun applyDisabledStyle(button: Button) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#E0E0E0")) // Color más claro para deshabilitado
            cornerRadius = 90f
        }
        button.background = drawable
        button.setTextColor(Color.parseColor("#AAAAAA")) // Texto más claro
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        button.compoundDrawablePadding = 0
    }

    fun setOnSelectionChangedListener(listener: (position: Int, value: String) -> Unit) {
        onSelectionChangedListener = listener
    }

    fun getSelectedPosition(): Int = selectedPosition

    fun getSelectedValue(): String? {
        return if (selectedPosition in 0 until buttonValues.size) {
            buttonValues[selectedPosition].label
        } else null
    }

    // Método para habilitar/deshabilitar botones dinámicamente
    fun updateButtonState(position: Int, disabled: Boolean) {
        if (position in 0 until buttonValues.size) {
            buttonValues[position] = buttonValues[position].copy(disabled = disabled)
            buttons[position].isEnabled = !disabled

            if (disabled && position == selectedPosition) {
                // Si el botón seleccionado se deshabilita, seleccionar otro
                val firstEnabledIndex = buttonValues.indexOfFirst { !it.disabled }
                if (firstEnabledIndex != -1) {
                    selectButton(firstEnabledIndex)
                } else {
                    selectedPosition = -1
                }
                applyDisabledStyle(buttons[position])
            } else if (disabled) {
                applyDisabledStyle(buttons[position])
            } else {
                if (position == selectedPosition) {
                    applySelectedStyle(buttons[position])
                } else {
                    applyUnselectedStyle(buttons[position])
                }
            }
        }
    }
}