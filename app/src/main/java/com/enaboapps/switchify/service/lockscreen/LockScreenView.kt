package com.enaboapps.switchify.service.lockscreen

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.enaboapps.switchify.R
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

class LockScreenView {
    private lateinit var baseLayout: LinearLayout
    private lateinit var preferenceManager: PreferenceManager
    private var showing = false

    fun setup(context: Context) {
        baseLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(context.resources.getColor(R.color.navy, null))
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        preferenceManager = PreferenceManager(context)
    }

    fun show(context: Context) {
        if (showing || !isLockScreenEnabled()) {
            return
        }

        disposeOfLockScreenLayout()
        buildLockScreenLayout(context)
        showing = true

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,  // Allow watching outside touches
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        params.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        SwitchifyAccessibilityWindow.instance.addViewUnderBase(baseLayout, params)
    }

    fun hide() {
        if (!showing) {
            return
        }

        // Remove the base layout from the window
        try {
            SwitchifyAccessibilityWindow.instance.removeViewFromWindow(baseLayout)
            showing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disposeOfLockScreenLayout() {
        baseLayout.removeAllViews()
    }

    private fun buildLockScreenLayout(context: Context) {
        if (isLockScreenCodeSet()) {
            val textView = TextView(context).apply {
                text = "Enter the lock screen code to unlock Switchify"
                setTextColor(context.resources.getColor(R.color.white, null))
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 48)
            }
            baseLayout.addView(textView)

            val codeInput = EditText(context).apply {
                setTextColor(context.resources.getColor(R.color.white, null))
                textSize = 32f
                gravity = Gravity.CENTER
                inputType = InputType.TYPE_CLASS_NUMBER
                imeOptions = EditorInfo.IME_ACTION_DONE
                background = null
                hint = "Enter code"
                setHintTextColor(context.resources.getColor(R.color.white, null))
                layoutParams = LinearLayout.LayoutParams(
                    500,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val enteredCode = text.toString()
                        if (validateLockScreenCode(enteredCode)) {
                            hide()
                            // Dismiss keyboard
                            val imm =
                                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(windowToken, 0)
                        }
                        true
                    } else {
                        false
                    }
                }

                // Request focus when view is created
                requestFocus()
            }
            baseLayout.addView(codeInput)
        } else {
            val textView = TextView(context).apply {
                text = "Tap the button to unlock Switchify"
                setTextColor(context.resources.getColor(R.color.white, null))
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 48)
            }
            baseLayout.addView(textView)

            val button = Button(context).apply {
                text = "Unlock"
                setTextColor(context.resources.getColor(R.color.white, null))
                textSize = 20f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    400,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setOnClickListener {
                    hide()
                }
            }
            baseLayout.addView(button)
        }
    }

    private fun isLockScreenEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN)
    }

    private fun isLockScreenCodeSet(): Boolean {
        return preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN_CODE) != ""
    }

    private fun validateLockScreenCode(code: String): Boolean {
        return code == preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN_CODE)
    }
}