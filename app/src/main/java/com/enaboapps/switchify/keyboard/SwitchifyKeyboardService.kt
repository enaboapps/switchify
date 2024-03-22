package com.enaboapps.switchify.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.enaboapps.switchify.R

/**
 * This class is responsible for managing the keyboard service.
 * It extends InputMethodService and implements KeyboardLayoutListener.
 */
class SwitchifyKeyboardService : InputMethodService(), KeyboardLayoutListener {

    // The main keyboard layout
    private lateinit var keyboardLayout: LinearLayout

    // The keyboard accessibility manager
    private lateinit var keyboardAccessibilityManager: KeyboardAccessibilityManager

    // The global layout listener
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    /**
     * This method is called when the input view is created.
     * It initializes the keyboard layout and the keyboard accessibility manager.
     */
    override fun onCreateInputView(): View {
        // Create the main keyboard layout programmatically
        keyboardLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            background = resources.getDrawable(R.drawable.keyboard_background, null)
        }

        // Initialize the keyboard accessibility manager
        keyboardAccessibilityManager = KeyboardAccessibilityManager(this)

        // Set the layout listener
        KeyboardLayoutManager.listener = this

        // Initialize the keyboard layout
        initializeKeyboardLayout(keyboardLayout)
        return keyboardLayout
    }

    /**
     * This method is called when the input view is started.
     * It updates the keyboard layout and adds the global layout listener.
     */
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Add the global layout listener when the input view is started
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            keyboardAccessibilityManager.captureAndBroadcastLayoutInfo(keyboardLayout)
        }
        keyboardLayout.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    /**
     * This method is called when the input view is finished.
     * It removes the global layout listener.
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        // Remove the global layout listener when the input view is finished
        keyboardLayout.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

    /**
     * This method initializes the keyboard layout.
     * It creates a new row layout for each row in the current layout,
     * and a new key button for each key type in the row.
     */
    private fun initializeKeyboardLayout(keyboardLayout: LinearLayout) {
        KeyboardLayoutManager.currentLayout.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val buttonLayoutParams =
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            // The third parameter (weight) ensures each button takes equal space within its row.

            row.forEach { keyType ->
                val keyButton = KeyboardKey(this).apply {
                    text = keyType.toString()
                    setOnClickListener { handleKeyPress(keyType) }
                    layoutParams = buttonLayoutParams
                }
                rowLayout.addView(keyButton)
            }

            keyboardLayout.addView(rowLayout)
        }
    }

    /**
     * This method handles key press events.
     * It performs different actions based on the type of the key.
     */
    private fun handleKeyPress(keyType: KeyType) {
        when (keyType) {
            is KeyType.Character -> currentInputConnection.commitText(keyType.char, 1)
            KeyType.Backspace -> currentInputConnection.deleteSurroundingText(1, 0)
            KeyType.Space -> currentInputConnection.commitText(" ", 1)
            KeyType.Return -> currentInputConnection.commitText("\n", 1)
            KeyType.Shift -> KeyboardLayoutManager.toggleShift()
            KeyType.SwitchToSymbols -> KeyboardLayoutManager.switchLayout(KeyboardLayoutType.Symbols)
            KeyType.SwitchToAlphabetic -> KeyboardLayoutManager.switchLayout(KeyboardLayoutType.AlphabeticLower)
            else -> {} // Handle other key types as necessary
        }
    }

    /**
     * This method is called when the keyboard layout changes.
     * It updates the keyboard layout.
     */
    override fun onLayoutChanged(layoutType: KeyboardLayoutType) {
        // Update the keyboard layout when the layout changes
        keyboardLayout.removeAllViews()
        initializeKeyboardLayout(keyboardLayout)
    }
}