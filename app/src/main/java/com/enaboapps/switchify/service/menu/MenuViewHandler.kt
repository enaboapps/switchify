package com.enaboapps.switchify.service.menu

import android.util.Log
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

class MenuViewHandler {

    companion object {
        val instance: MenuViewHandler by lazy { MenuViewHandler() }

        private const val TAG = "MenuViewHandler"
        private const val VIEW_ID = 1512
    }

    /** The base layout for the menu. */
    private var baseLayout: RelativeLayout? = null

    fun setup(context: android.content.Context) {
        if (!isSetup()) {
            baseLayout = RelativeLayout(context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                id = VIEW_ID
            }
            SwitchifyAccessibilityWindow.instance.addView(baseLayout!!, 0, 0)
        }
    }

    private fun isSetup(): Boolean {
        return baseLayout != null
    }

    fun addViewOffScreen(view: android.view.View) {
        try {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            params.leftMargin = Int.MAX_VALUE
            params.topMargin = Int.MAX_VALUE
            baseLayout?.addView(view, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error in addView: ${e.message}", e)
        }
    }

    fun updateView(view: android.view.View, x: Int, y: Int, width: Int, height: Int) {
        try {
            val params = view.layoutParams as RelativeLayout.LayoutParams
            params.leftMargin = x
            params.topMargin = y
            params.width = width
            params.height = height
            view.layoutParams = params
            baseLayout?.updateViewLayout(view, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateView: ${e.message}", e)
        }
    }

    fun kill() {
        if (isSetup()) {
            SwitchifyAccessibilityWindow.instance.removeView(VIEW_ID)
        }
        baseLayout?.removeAllViews()
        baseLayout = null
    }
}