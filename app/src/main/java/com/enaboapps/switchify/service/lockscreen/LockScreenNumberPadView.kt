package com.enaboapps.switchify.service.lockscreen

import android.content.Context
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout

class LockScreenNumberPadView(context: Context) : LinearLayout(context) {

    private var onNumberClickListener: ((String) -> Unit)? = null
    private var onDeleteClickListener: (() -> Unit)? = null

    companion object {
        private const val BUTTON_SIZE = 150
    }

    init {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER

        val gridLayout = GridLayout(context).apply {
            rowCount = 4
            columnCount = 3
            useDefaultMargins = true
        }

        // Add number buttons (1-9)
        for (i in 1..9) {
            val button = createButton(i.toString())
            val row = (i - 1) / 3
            val col = (i - 1) % 3
            val params = GridLayout.LayoutParams().apply {
                width = BUTTON_SIZE  // Fixed width for all buttons
                height = BUTTON_SIZE // Fixed height for all buttons
                setMargins(8, 8, 8, 8)
                rowSpec = GridLayout.spec(row)
                columnSpec = GridLayout.spec(col)
            }
            gridLayout.addView(button, params)
        }

        // Add 0 button
        val zeroButton = createButton("0")
        val zeroParams = GridLayout.LayoutParams().apply {
            width = BUTTON_SIZE
            height = BUTTON_SIZE
            setMargins(8, 8, 8, 8)
            rowSpec = GridLayout.spec(3)
            columnSpec = GridLayout.spec(1)
        }
        gridLayout.addView(zeroButton, zeroParams)

        // Add delete button
        val deleteButton = createButton("⌫").apply {
            setOnClickListener {
                onDeleteClickListener?.invoke()
            }
        }
        val deleteParams = GridLayout.LayoutParams().apply {
            width = BUTTON_SIZE
            height = BUTTON_SIZE
            setMargins(8, 8, 8, 8)
            rowSpec = GridLayout.spec(3)
            columnSpec = GridLayout.spec(2)
        }
        gridLayout.addView(deleteButton, deleteParams)

        addView(gridLayout)
    }

    private fun createButton(text: String): Button {
        return Button(context).apply {
            this.text = text
            textSize = 24f
            setTextColor(context.resources.getColor(android.R.color.white, null))
            setBackgroundColor(context.resources.getColor(android.R.color.transparent, null))

            setOnClickListener {
                if (text != "⌫") {
                    onNumberClickListener?.invoke(text)
                }
            }
        }
    }

    fun setOnNumberClickListener(listener: (String) -> Unit) {
        onNumberClickListener = listener
    }

    fun setOnDeleteClickListener(listener: () -> Unit) {
        onDeleteClickListener = listener
    }
} 