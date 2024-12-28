package com.enaboapps.switchify.service.switches.camera

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.switches.external.SwitchEventProvider
import com.enaboapps.switchify.service.window.ServiceMessageHUD
import com.enaboapps.switchify.switches.CameraSwitchFacialGesture
import com.enaboapps.switchify.switches.SwitchEvent
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class CameraSwitchManager(
    private val context: Context,
    private val scanningManager: ScanningManager
) {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private var lastSmileState = false
    private var lastLeftEyeState = true
    private var lastRightEyeState = true
    private var lastBlinkState = true

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.2f)
            .build()
    )

    fun startCamera(lifecycleOwner: LifecycleOwner) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindPreview(lifecycleOwner)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
                ServiceMessageHUD.instance.showMessage(
                    "Camera Error: ${e.message}",
                    ServiceMessageHUD.MessageType.DISAPPEARING,
                    ServiceMessageHUD.Time.SHORT
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun bindPreview(lifecycleOwner: LifecycleOwner) {
        preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            Log.d(TAG, "Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera use cases", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val leftEyeOpen = (face.leftEyeOpenProbability ?: 1f) > EYE_OPEN_THRESHOLD
                        val rightEyeOpen = (face.rightEyeOpenProbability ?: 1f) > EYE_OPEN_THRESHOLD

                        // Detect smile
                        val isSmiling = (face.smilingProbability ?: 0f) > SMILE_THRESHOLD
                        if (isSmiling != lastSmileState) {
                            Log.d(TAG, "Smiling: $isSmiling")
                            lastSmileState = isSmiling
                            if (isSmiling) {
                                gestureStarted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.SMILE))
                            } else {
                                gestureCompleted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.SMILE))
                            }
                        }

                        // Detect left wink
                        if (leftEyeOpen != lastLeftEyeState && rightEyeOpen) {
                            Log.d(TAG, "Left eye open")
                            lastLeftEyeState = leftEyeOpen
                            if (!leftEyeOpen) {
                                gestureStarted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.LEFT_WINK))
                            } else {
                                gestureCompleted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.LEFT_WINK))
                            }
                        }

                        // Detect right wink
                        if (rightEyeOpen != lastRightEyeState && leftEyeOpen) {
                            Log.d(TAG, "Right eye open")
                            lastRightEyeState = rightEyeOpen
                            if (!rightEyeOpen) {
                                gestureStarted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.RIGHT_WINK))
                            } else {
                                gestureCompleted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.RIGHT_WINK))
                            }
                        }

                        // Detect blink
                        val bothEyesOpen = leftEyeOpen && rightEyeOpen
                        if (bothEyesOpen != lastBlinkState) {
                            Log.d(TAG, "Both eyes open: $bothEyesOpen")
                            lastBlinkState = bothEyesOpen
                            if (!bothEyesOpen) {
                                gestureStarted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.BLINK))
                            } else {
                                gestureCompleted(CameraSwitchFacialGesture(CameraSwitchFacialGesture.BLINK))
                            }
                        }

                        Log.d(TAG, "Last smile state: $lastSmileState")
                        Log.d(TAG, "Last left eye state: $lastLeftEyeState")
                        Log.d(TAG, "Last right eye state: $lastRightEyeState")
                        Log.d(TAG, "Last blink state: $lastBlinkState")
                    } else {
                        Log.d(TAG, "No faces detected")
                        reset()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection failed", e)
                    reset()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun reset() {
        lastSmileState = false
        lastLeftEyeState = true
        lastRightEyeState = true
        lastBlinkState = true

        scanningManager.resumeScanning()
    }

    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop camera", e)
        }
    }

    /**
     * This function is called when a gesture is started.
     */
    private fun gestureStarted(gesture: CameraSwitchFacialGesture) {
        Log.d(TAG, "Gesture started: $gesture")
        // Find the associated switch event for the gesture
        val switchEvent = findSwitchEventForGesture(gesture)
        if (switchEvent != null) {
            scanningManager.pauseScanning()
        }
    }

    /**
     * This function is called when a gesture is completed.
     */
    private fun gestureCompleted(gesture: CameraSwitchFacialGesture) {
        Log.d(TAG, "Gesture completed: $gesture")

        // Find the associated switch event for the gesture
        val switchEvent = findSwitchEventForGesture(gesture)
        if (switchEvent != null) {
            // Perform the switch action
            scanningManager.performAction(switchEvent.pressAction)
            scanningManager.resumeScanning()
        }
    }

    /**
     * This function finds the associated switch event for a given gesture.
     *
     * @param gesture The gesture to find the switch event for.
     * @return The switch event associated with the gesture, or null if not found.
     */
    private fun findSwitchEventForGesture(gesture: CameraSwitchFacialGesture): SwitchEvent? {
        return SwitchEventProvider.findCamera(gesture.id)
    }

    companion object {
        private const val TAG = "CameraSwitchManager"
        private const val SMILE_THRESHOLD = 0.7f
        private const val EYE_OPEN_THRESHOLD = 0.5f
    }
}