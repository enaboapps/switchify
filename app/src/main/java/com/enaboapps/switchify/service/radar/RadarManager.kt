package com.enaboapps.switchify.service.radar

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.selection.AutoSelectionHandler
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RadarManager(private val context: Context) : ScanStateInterface {

    companion object {
        private const val TAG = "RadarManager"
        private const val FULL_CIRCLE = 360f
        private const val ROTATION_STEP = 360f / 36  // 10 degrees per step
        private const val MOVEMENT_STEP = 0.05f  // 5% of max distance per step
    }

    enum class RadarStep {
        IDLE,
        ROTATING,
        MOVING
    }

    enum class RotationDirection {
        CLOCKWISE,
        ANTI_CLOCKWISE
    }

    enum class CircleMovement {
        OUTWARD,
        INWARD
    }

    private val scanSettings = ScanSettings(context)
    private val uiHandler = Handler(Looper.getMainLooper())
    private val radarUI = RadarUI(context, uiHandler)

    private var currentAngle = 0f
    private var currentDistanceRatio = 0f
    private var scanningScheduler: ScanningScheduler? = null

    private val screenCenterX: Float
        get() = ScreenUtils.getWidth(context) / 2f
    private val screenCenterY: Float
        get() = ScreenUtils.getHeight(context) / 2f
    private val maxDistance: Float
        get() {
            val width = ScreenUtils.getWidth(context).toFloat()
            val height = ScreenUtils.getHeight(context).toFloat()
            return sqrt(width * width + height * height) / 2f
        }

    private var currentStep = RadarStep.IDLE
    private var rotationDirection = RotationDirection.CLOCKWISE
    private var circleMovement = CircleMovement.OUTWARD

    init {
        setup()
    }

    private fun setup() {
        if (scanningScheduler == null) {
            scanningScheduler = ScanningScheduler(context) { update() }
        }
    }

    private fun update() {
        when (currentStep) {
            RadarStep.ROTATING -> rotate()
            RadarStep.MOVING -> moveCircle()
            RadarStep.IDLE -> {} // Do nothing
        }
    }

    private fun rotate() {
        currentAngle = when (rotationDirection) {
            RotationDirection.CLOCKWISE -> (currentAngle + ROTATION_STEP) % FULL_CIRCLE
            RotationDirection.ANTI_CLOCKWISE -> (currentAngle - ROTATION_STEP + FULL_CIRCLE) % FULL_CIRCLE
        }
        updateRadarLine()
    }

    private fun moveCircle() {
        when (circleMovement) {
            CircleMovement.OUTWARD -> {
                currentDistanceRatio += MOVEMENT_STEP
                if (currentDistanceRatio > 1f) {
                    currentDistanceRatio = 1f
                    circleMovement =
                        CircleMovement.INWARD  // Reverse direction when reaching the edge
                }
            }

            CircleMovement.INWARD -> {
                currentDistanceRatio -= MOVEMENT_STEP
                if (currentDistanceRatio < 0f) {
                    currentDistanceRatio = 0f
                    circleMovement =
                        CircleMovement.OUTWARD  // Reverse direction when reaching the center
                }
            }
        }
        updateRadarCircle()
    }

    private fun updateRadarLine() {
        radarUI.showRadarLine(currentAngle)
    }

    private fun updateRadarCircle() {
        val angle = Math.toRadians(currentAngle.toDouble())
        val distance = currentDistanceRatio * maxDistance
        val x = screenCenterX + distance * cos(angle).toFloat()
        val y = screenCenterY + distance * sin(angle).toFloat()
        radarUI.showRadarCircle(x.toInt(), y.toInt())
    }

    private fun startRadar() {
        if (currentStep == RadarStep.IDLE) {
            currentStep = RadarStep.ROTATING
            updateRadarLine()
            startAutoScanIfEnabled()
        }
    }

    private fun startAutoScanIfEnabled() {
        if (scanSettings.isAutoScanMode()) {
            val rate = scanSettings.getScanRate()
            scanningScheduler?.startScanning(rate, rate)
        }
    }

    fun manualNextStep() {
        rotationDirection = RotationDirection.CLOCKWISE
        circleMovement = CircleMovement.OUTWARD
        if (currentStep == RadarStep.IDLE) {
            currentStep = RadarStep.ROTATING
        }

        when (currentStep) {
            RadarStep.ROTATING -> {
                rotate()
            }

            RadarStep.MOVING -> {
                moveCircle()
            }

            RadarStep.IDLE -> {} // Do nothing
        }
    }

    fun manualPreviousStep() {
        rotationDirection = RotationDirection.ANTI_CLOCKWISE
        circleMovement = CircleMovement.INWARD
        if (currentStep == RadarStep.IDLE) {
            currentStep = RadarStep.ROTATING
        }

        when (currentStep) {
            RadarStep.ROTATING -> {
                rotate()
            }

            RadarStep.MOVING -> {
                moveCircle()
            }

            RadarStep.IDLE -> {} // Do nothing
        }
    }

    override fun stopScanning() {
        scanningScheduler?.stopScanning()
    }

    override fun pauseScanning() {
        scanningScheduler?.pauseScanning()
    }

    override fun resumeScanning() {
        scanningScheduler?.resumeScanning()
    }

    fun performSelectionAction() {
        setup()

        when (currentStep) {
            RadarStep.ROTATING -> {
                currentStep = RadarStep.MOVING
                currentDistanceRatio = if (circleMovement == CircleMovement.OUTWARD) 0f else 1f
                radarUI.removeRadarLine()  // Remove the line when angle is selected
                updateRadarCircle()  // Show the circle at the start of the line
                startAutoScanIfEnabled()
            }

            RadarStep.MOVING -> {
                stopScanning()
                val angle = Math.toRadians(currentAngle.toDouble())
                val distance = currentDistanceRatio * maxDistance
                val x = screenCenterX + distance * cos(angle).toFloat()
                val y = screenCenterY + distance * sin(angle).toFloat()
                GesturePoint.x = x.toInt()
                GesturePoint.y = y.toInt()
                AutoSelectionHandler.setSelectAction { performTapAction() }
                AutoSelectionHandler.performSelectionAction()
                resetRadar()
            }

            RadarStep.IDLE -> {
                startRadar()
            }
        }
    }

    private fun performTapAction() {
        GestureManager.getInstance().performTap()
    }

    fun resetRadar() {
        currentStep = RadarStep.IDLE
        currentAngle = 0f
        currentDistanceRatio = 0f
        rotationDirection = RotationDirection.CLOCKWISE
        circleMovement = CircleMovement.OUTWARD
        radarUI.reset()
        stopScanning()
    }

    fun cleanup() {
        radarUI.reset()
        scanningScheduler?.shutdown()
        scanningScheduler = null
    }

    private fun toggleRotationDirection() {
        rotationDirection = when (rotationDirection) {
            RotationDirection.CLOCKWISE -> RotationDirection.ANTI_CLOCKWISE
            RotationDirection.ANTI_CLOCKWISE -> RotationDirection.CLOCKWISE
        }
    }

    private fun toggleCircleMovement() {
        circleMovement = when (circleMovement) {
            CircleMovement.OUTWARD -> CircleMovement.INWARD
            CircleMovement.INWARD -> CircleMovement.OUTWARD
        }
    }

    fun toggleDirection() {
        when (currentStep) {
            RadarStep.ROTATING -> toggleRotationDirection()
            RadarStep.MOVING -> toggleCircleMovement()
            RadarStep.IDLE -> {}  // Do nothing
        }
    }
}