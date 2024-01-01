package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.utils.ScreenUtils
import java.util.*

class CursorManager(private val context: Context) {

    private val TAG = "CursorManager"

    private val cursorLineThickness = 10

    private val preferenceManager: PreferenceManager = PreferenceManager(context)

    private var windowManager: WindowManager? = null

    private var xQuadrantParams: WindowManager.LayoutParams? = null
    private var xQuadrant: LinearLayout? = null
    private var yQuadrantParams: WindowManager.LayoutParams? = null
    private var yQuadrant: LinearLayout? = null

    private var xCursorLineParams: WindowManager.LayoutParams? = null
    private var xCursorLine: LinearLayout? = null
    private var yCursorLineParams: WindowManager.LayoutParams? = null
    private var yCursorLine: LinearLayout? = null

    private var isInQuadrant = false
    private var quadrantInfo: QuadrantInfo? = null

    private var x: Int = 0
    private var y: Int = 0

    private var direction: Direction = Direction.RIGHT

    private var movingTimer: Timer? = null // Timer to move the cursor line

    // auto select variables
    private var isInAutoSelect = false // If true, we listen for a second event to activate the menu
    private var autoSelectTimer: Timer? = null // Timer to wait for the second event

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }


    fun setup() {
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        xQuadrantParams = WindowManager.LayoutParams()
        yQuadrantParams = WindowManager.LayoutParams()
        xCursorLineParams = WindowManager.LayoutParams()
        yCursorLineParams = WindowManager.LayoutParams()
    }


    private fun setupYQuadrant() {
        if (yQuadrant == null) {
            y = 0
            yQuadrant = LinearLayout(context)
            yQuadrant?.setBackgroundColor(Color.RED)
            yQuadrant?.alpha = 0.5f
            yQuadrantParams?.y = y
            yQuadrantParams?.gravity = Gravity.START or Gravity.TOP
            yQuadrantParams?.width = ScreenUtils.getWidth(context)
            yQuadrantParams?.height = ScreenUtils.getHeight(context) / 4
            yQuadrantParams?.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            yQuadrantParams?.format = PixelFormat.TRANSLUCENT
            windowManager?.addView(yQuadrant, yQuadrantParams)
        }
    }

    private fun setupXQuadrant() {
        if (xQuadrant == null) {
            x = 0
            xQuadrant = LinearLayout(context)
            xQuadrant?.setBackgroundColor(Color.RED)
            xQuadrant?.alpha = 0.5f
            xQuadrantParams?.x = x
            xQuadrantParams?.gravity = Gravity.START or Gravity.TOP
            xQuadrantParams?.width = ScreenUtils.getWidth(context) / 4
            xQuadrantParams?.height = ScreenUtils.getHeight(context)
            xQuadrantParams?.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            xQuadrantParams?.format = PixelFormat.TRANSLUCENT
            windowManager?.addView(xQuadrant, xQuadrantParams)
        }
    }


    private fun setupYCursorLine() {
        if (yCursorLine == null) {
            quadrantInfo = QuadrantInfo(y, y + ScreenUtils.getHeight(context) / 4)
            yCursorLine = LinearLayout(context)
            yCursorLine?.setBackgroundColor(Color.RED)
            yCursorLineParams?.y = quadrantInfo?.start
            yCursorLineParams?.gravity = Gravity.START or Gravity.TOP
            yCursorLineParams?.width = ScreenUtils.getWidth(context)
            yCursorLineParams?.height = cursorLineThickness
            yCursorLineParams?.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            yCursorLineParams?.format = PixelFormat.TRANSPARENT
            yCursorLineParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager?.addView(yCursorLine, yCursorLineParams)
        }
    }

    private fun setupXCursorLine() {
        if (xCursorLine == null) {
            quadrantInfo = QuadrantInfo(x, x + ScreenUtils.getWidth(context) / 4)
            xCursorLine = LinearLayout(context)
            xCursorLine?.setBackgroundColor(Color.RED)
            xCursorLineParams?.x = quadrantInfo?.start
            xCursorLineParams?.gravity = Gravity.START or Gravity.TOP
            xCursorLineParams?.width = cursorLineThickness
            xCursorLineParams?.height = ScreenUtils.getHeight(context)
            xCursorLineParams?.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            xCursorLineParams?.format = PixelFormat.TRANSPARENT
            xCursorLineParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager?.addView(xCursorLine, xCursorLineParams)
        }
    }


    private fun start() {
        var rate =
            preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        if (isInQuadrant && rate > 200) {
            rate = 200
        }
        Log.d(TAG, "start: $rate")
        val handler = Handler(Looper.getMainLooper())
        if (movingTimer == null) {
            movingTimer = Timer()
            movingTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (isInQuadrant) {
                            moveCursorLine()
                        } else {
                            moveToNextQuadrant()
                        }
                    }
                }
            }, rate.toLong(), rate.toLong())
        }
    }


    // Function to stop the timer
    private fun stop() {
        movingTimer?.cancel()
        movingTimer = null
    }


    // Function to move to the next quadrant
    private fun moveToNextQuadrant() {
        when (direction) {
            Direction.LEFT -> {
                if (x > 0) {
                    if (xQuadrant != null) {
                        x -= ScreenUtils.getWidth(context) / 4
                        xQuadrantParams?.x = x
                        windowManager?.updateViewLayout(xQuadrant, xQuadrantParams)
                    }
                } else {
                    direction = Direction.RIGHT
                    moveToNextQuadrant()
                }
            }
            Direction.RIGHT -> {
                if (x < ScreenUtils.getWidth(context) - ScreenUtils.getWidth(context) / 4) {
                    if (xQuadrant != null) {
                        x += ScreenUtils.getWidth(context) / 4
                        xQuadrantParams?.x = x
                        windowManager?.updateViewLayout(xQuadrant, xQuadrantParams)
                    }
                } else {
                    direction = Direction.LEFT
                    moveToNextQuadrant()
                }
            }
            Direction.UP -> {
                if (y > 0) {
                    if (yQuadrant != null) {
                        y -= ScreenUtils.getHeight(context) / 4
                        yQuadrantParams?.y = y
                        windowManager?.updateViewLayout(yQuadrant, yQuadrantParams)
                    }
                } else {
                    direction = Direction.DOWN
                    moveToNextQuadrant()
                }
            }
            Direction.DOWN -> {
                if (y < ScreenUtils.getHeight(context) - ScreenUtils.getHeight(context) / 4) {
                    if (yQuadrant != null) {
                        y += ScreenUtils.getHeight(context) / 4
                        yQuadrantParams?.y = y
                        windowManager?.updateViewLayout(yQuadrant, yQuadrantParams)
                    }
                } else {
                    direction = Direction.UP
                    moveToNextQuadrant()
                }
            }
        }
    }


    // Function to move the cursor line
    private fun moveCursorLine() {
        if (quadrantInfo != null) {
            when (direction) {
                Direction.LEFT ->
                    if (x > quadrantInfo?.start!!) {
                        if (xCursorLine != null) {
                            x -= cursorLineThickness * 2
                            xCursorLineParams?.x = x
                            windowManager?.updateViewLayout(xCursorLine, xCursorLineParams)
                        }
                    } else {
                        direction = Direction.RIGHT
                        moveCursorLine()
                    }
                Direction.RIGHT ->
                    if (x < quadrantInfo?.end!!) {
                        if (xCursorLine != null) {
                            x += cursorLineThickness * 2
                            xCursorLineParams?.x = x
                            windowManager?.updateViewLayout(xCursorLine, xCursorLineParams)
                        }
                    } else {
                        direction = Direction.LEFT
                        moveCursorLine()
                    }
                Direction.UP ->
                    if (y > quadrantInfo?.start!!) {
                        if (yCursorLine != null) {
                            y -= cursorLineThickness * 2
                            yCursorLineParams?.y = y
                            windowManager?.updateViewLayout(yCursorLine, yCursorLineParams)
                        }
                    } else {
                        direction = Direction.DOWN
                        moveCursorLine()
                    }
                Direction.DOWN ->
                    if (y < quadrantInfo?.end!!) {
                        if (yCursorLine != null) {
                            y += cursorLineThickness * 2
                            yCursorLineParams?.y = y
                            windowManager?.updateViewLayout(yCursorLine, yCursorLineParams)
                        }
                    } else {
                        direction = Direction.UP
                        moveCursorLine()
                    }
            }
        }
    }

    private fun reset() {
        stop()

        x = 0
        y = 0

        direction = Direction.RIGHT

        resetQuadrants()
        resetCursorLines()
    }

    private fun resetQuadrants() {
        if (xQuadrant != null) {
            windowManager?.removeView(xQuadrant)
        }
        if (yQuadrant != null) {
            windowManager?.removeView(yQuadrant)
        }
        xQuadrant = null
        yQuadrant = null
    }

    private fun resetCursorLines() {
        if (xCursorLine != null) {
            windowManager?.removeView(xCursorLine)
        }
        if (yCursorLine != null) {
            windowManager?.removeView(yCursorLine)
        }
        xCursorLine = null
        yCursorLine = null
    }


    fun performAction() {
        // If the event is triggered within the auto select delay, we don't perform the action
        if (checkAutoSelectDelay()) {
            return
        }

        // If moving timer is null, we start the timer and return
        if (movingTimer == null) {
            setupXQuadrant()
            start()
            return
        }

        // We perform the action based on the direction
        when (direction) {
            Direction.LEFT, Direction.RIGHT -> {
                stop()
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = Direction.RIGHT

                    resetQuadrants()

                    if (xCursorLine == null) {
                        setupXCursorLine()
                    }
                } else {
                    direction = Direction.DOWN
                    isInQuadrant = false

                    if (xQuadrant == null) {
                        setupYQuadrant()
                    }
                }
                start()
            }
            Direction.UP, Direction.DOWN -> {
                stop()
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = Direction.DOWN

                    resetQuadrants()

                    if (yCursorLine == null) {
                        setupYCursorLine()
                    }

                    start()
                } else {
                    isInQuadrant = false

                    performFinalAction()
                }
            }
        }
    }


    private fun performFinalAction() {
        val point = PointF(
            (x + (cursorLineThickness / 2)).toFloat(),
            (y + (cursorLineThickness / 2)).toFloat()
        )
        GestureManager.getInstance().currentPoint = point
        val auto = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
        if (auto && !isInAutoSelect) {
            startAutoSelectTimer()
        }
        reset()
    }


    // Function to check if the event is triggered within the auto select delay
    private fun checkAutoSelectDelay(): Boolean {
        if (isInAutoSelect) {
            isInAutoSelect = false
            autoSelectTimer?.cancel()
            autoSelectTimer = null
            // open menu
            MenuManager.getInstance().openMainMenu()
            return true
        }
        return false
    }


    // Function to start auto select timer
    private fun startAutoSelectTimer() {
        val delay =
            preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
        val handler = Handler(Looper.getMainLooper())
        isInAutoSelect = true
        if (autoSelectTimer == null) {
            autoSelectTimer = Timer()
            autoSelectTimer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (isInAutoSelect) {
                            isInAutoSelect = false
                            // tap
                            GestureManager.getInstance().performTap()
                        }
                    }
                }
            }, delay.toLong())
        }
    }


    // Function to draw a circle at x, y and remove after half a second
    private fun drawCircleAndRemove() {
        val circleSize = cursorLineThickness * 2

        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setColor(Color.RED)
        gradientDrawable.setSize(circleSize, circleSize)

        val circle = ImageView(context)
        circle.setImageDrawable(gradientDrawable)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.x = x - cursorLineThickness / 2
        layoutParams.y = y - cursorLineThickness / 2
        layoutParams.width = circleSize
        layoutParams.height = circleSize
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.format = PixelFormat.TRANSPARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager?.addView(circle, layoutParams)

        // Remove the circle after half a second
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager?.removeView(circle)
        }, 500)
    }

}

data class QuadrantInfo(
    val start: Int,
    val end: Int,
)