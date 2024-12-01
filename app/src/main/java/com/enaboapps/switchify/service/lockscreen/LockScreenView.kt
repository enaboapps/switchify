package com.enaboapps.switchify.service.lockscreen

import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
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
                text = "Enter 4-digit code to unlock Switchify"
                setTextColor(context.resources.getColor(R.color.white, null))
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 48)
            }
            baseLayout.addView(textView)

            val codeInput = TextView(context).apply {
                setTextColor(context.resources.getColor(R.color.white, null))
                textSize = 32f
                gravity = Gravity.CENTER
                text = ""
                letterSpacing = 0.5f
                layoutParams = LinearLayout.LayoutParams(
                    400,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 48)
                }
            }
            baseLayout.addView(codeInput)

            val numberPad = LockScreenNumberPadView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setOnNumberClickListener { number ->
                    val currentText = codeInput.text.toString()
                    if (currentText.length < 4) {
                        val newText = "•".repeat(currentText.length + 1)
                        codeInput.text = newText
                        
                        val actualCode = (codeInput.tag as? String ?: "") + number
                        codeInput.tag = actualCode
                        
                        if (actualCode.length == 4) {
                            if (validateLockScreenCode(actualCode)) {
                                hide()
                            } else {
                                // Wrong code handling
                                codeInput.startAnimation(createShakeAnimation())
                                codeInput.postDelayed({
                                    // Clear the input after animation
                                    codeInput.text = ""
                                    codeInput.tag = ""
                                }, 500)
                            }
                        }
                    }
                }

                setOnDeleteClickListener {
                    val currentText = codeInput.text.toString()
                    if (currentText.isNotEmpty()) {
                        val newText = "•".repeat(currentText.length - 1)
                        codeInput.text = newText
                        
                        // Update actual code in tag
                        val actualCode = (codeInput.tag as? String ?: "")
                        if (actualCode.isNotEmpty()) {
                            codeInput.tag = actualCode.substring(0, actualCode.length - 1)
                        }
                    }
                }
            }
            baseLayout.addView(numberPad)
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
        val savedCode = preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_LOCK_SCREEN_CODE)
        return code.length == 4 && code == savedCode
    }

    private fun createShakeAnimation(): Animation {
        return TranslateAnimation(0f, 10f, 0f, 0f).apply {
            duration = 50
            repeatMode = Animation.REVERSE
            repeatCount = 5
        }
    }
}