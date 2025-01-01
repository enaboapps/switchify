package com.enaboapps.switchify.service.switches.camera

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.switches.SwitchEventProvider
import com.enaboapps.switchify.service.window.ServiceMessageHUD
import com.enaboapps.switchify.switches.CameraSwitchFacialGesture
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraSwitchManager(
    private val context: Context,
    private val scanningManager: ScanningManager
) {
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null

    private data class CameraSwitchState(
        var isActive: Boolean,
        var startTime: Long = 0
    )

    private val gestureStates = mutableMapOf(
        CameraSwitchFacialGesture.SMILE to CameraSwitchState(false),
        CameraSwitchFacialGesture.LEFT_WINK to CameraSwitchState(true),
        CameraSwitchFacialGesture.RIGHT_WINK to CameraSwitchState(true),
        CameraSwitchFacialGesture.BLINK to CameraSwitchState(true)
    )

    // Track currently active gesture
    private var activeGesture: String? = null

    private data class FaceState(
        var leftEyeOpen: Boolean = true,
        var rightEyeOpen: Boolean = true,
        var isSmiling: Boolean = false
    )

    private val currentFaceState = FaceState()
    private var lastProcessedState = FaceState()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(MIN_FACE_SIZE)
            .enableTracking()  // Enable face tracking for better performance
            .build()
    )

    fun startCamera(lifecycleOwner: LifecycleOwner) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                try {
                    cameraProvider = future.get()
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
    }

    @OptIn(ExperimentalGetImage::class)
    private fun bindPreview(lifecycleOwner: LifecycleOwner) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
            .apply {
                setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    processImageSafely(imageProxy)
                }
            }

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                Preview.Builder().build().also { preview = it },
                imageAnalyzer
            )
            Log.d(TAG, "Camera bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera use cases", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageSafely(imageProxy: ImageProxy) {
        coroutineScope.launch(Dispatchers.Default) {
            try {
                imageProxy.image?.let { mediaImage ->
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    withContext(Dispatchers.Main) {
                        faceDetector.process(image)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    processFace(faces[0])
                                } else {
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
                    }
                } ?: imageProxy.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                imageProxy.close()
            }
        }
    }

    private fun processFace(face: Face) {
        // Update current face state
        currentFaceState.apply {
            leftEyeOpen = (face.leftEyeOpenProbability ?: 1f) > EYE_OPEN_THRESHOLD
            rightEyeOpen = (face.rightEyeOpenProbability ?: 1f) > EYE_OPEN_THRESHOLD
            isSmiling = (face.smilingProbability ?: 0f) > SMILE_THRESHOLD
        }

        // Debug logging for state tracking
        Log.v(
            TAG,
            "Face state - L:${currentFaceState.leftEyeOpen} R:${currentFaceState.rightEyeOpen} " +
                    "Active:${activeGesture} LastL:${lastProcessedState.leftEyeOpen} LastR:${lastProcessedState.rightEyeOpen}"
        )

        // Only process if the state has changed
        if (currentFaceState != lastProcessedState) {
            // Handle Smile
            if (currentFaceState.isSmiling != lastProcessedState.isSmiling && SwitchEventProvider.isFacialGestureAssigned(
                    CameraSwitchFacialGesture.SMILE
                )
            ) {
                handleGestureStateChange(
                    CameraSwitchFacialGesture(CameraSwitchFacialGesture.SMILE),
                    currentFaceState.isSmiling
                )
            }

            // Handle Left Wink (only when right eye is open)
            if (currentFaceState.leftEyeOpen != lastProcessedState.leftEyeOpen && currentFaceState.rightEyeOpen && SwitchEventProvider.isFacialGestureAssigned(
                    CameraSwitchFacialGesture.LEFT_WINK
                )
            ) {
                handleGestureStateChange(
                    CameraSwitchFacialGesture(CameraSwitchFacialGesture.LEFT_WINK),
                    !currentFaceState.leftEyeOpen
                )
            }

            // Handle Right Wink (only when left eye is open)
            if (currentFaceState.rightEyeOpen != lastProcessedState.rightEyeOpen && currentFaceState.leftEyeOpen && SwitchEventProvider.isFacialGestureAssigned(
                    CameraSwitchFacialGesture.RIGHT_WINK
                )
            ) {
                handleGestureStateChange(
                    CameraSwitchFacialGesture(CameraSwitchFacialGesture.RIGHT_WINK),
                    !currentFaceState.rightEyeOpen
                )
            }

            // Handle Blink
            val eyesClosed = !currentFaceState.leftEyeOpen && !currentFaceState.rightEyeOpen
            if (eyesClosed && SwitchEventProvider.isFacialGestureAssigned(
                    CameraSwitchFacialGesture.BLINK
                )
            ) {
                handleGestureStateChange(
                    CameraSwitchFacialGesture(CameraSwitchFacialGesture.BLINK),
                    true
                )
            } else if (!eyesClosed && SwitchEventProvider.isFacialGestureAssigned(
                    CameraSwitchFacialGesture.BLINK
                )
            ) {
                handleGestureStateChange(
                    CameraSwitchFacialGesture(CameraSwitchFacialGesture.BLINK),
                    false
                )
            }

            // Update last processed state
            lastProcessedState = currentFaceState.copy()
        }
    }

    private fun handleGestureStateChange(gesture: CameraSwitchFacialGesture, isStarting: Boolean) {
        if (isStarting) {
            gestureStarted(gesture)
        } else {
            // Only complete gestures that were actually started
            if (activeGesture == gesture.id) {
                gestureCompleted(gesture)
            }
        }
    }

    private fun reset() {
        gestureStates.forEach { (_, state) ->
            state.isActive = false
            state.startTime = 0
        }
        activeGesture = null
        lastProcessedState = FaceState()  // Reset the last processed state
        scanningManager.resumeScanning()
    }

    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            imageAnalyzer = null
            preview = null
            cameraProviderFuture = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop camera", e)
        }
    }

    private fun gestureStarted(gesture: CameraSwitchFacialGesture) {
        // Only start a new gesture if no other gesture is active
        if (activeGesture == null) {
            findSwitchEventForGesture(gesture)?.let { switchEvent ->
                gestureStates[switchEvent.code]?.apply {
                    isActive = true
                    startTime = System.currentTimeMillis()
                }
                activeGesture = switchEvent.code
                if (switchEvent.pressAction.id == SwitchAction.ACTION_SELECT) {
                    scanningManager.pauseScanning()
                }
                Log.d(TAG, "Activated gesture: ${switchEvent.code}")
            }
        } else {
            Log.v(TAG, "Ignored gesture ${gesture.id} - $activeGesture is already active")
        }
    }

    private fun gestureCompleted(gesture: CameraSwitchFacialGesture) {
        Log.d(TAG, "Gesture completed: ${gesture.id}")

        findSwitchEventForGesture(gesture)?.let { switchEvent ->
            // Only process completion if this is the active gesture
            if (activeGesture == switchEvent.code) {
                if (switchEvent.pressAction.id == SwitchAction.ACTION_SELECT) {
                    scanningManager.resumeScanning()
                }
                gestureStates[switchEvent.code]?.let { state ->
                    if (state.isActive && state.startTime > 0) {
                        val timeElapsed = System.currentTimeMillis() - state.startTime
                        if (timeElapsed >= switchEvent.facialGestureTime) {
                            scanningManager.performAction(switchEvent.pressAction)
                            Log.d(
                                TAG,
                                "${gesture.id} completed successfully after ${timeElapsed}ms"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "${gesture.id} interrupted after ${timeElapsed}ms (needed ${switchEvent.facialGestureTime}ms)"
                            )
                        }
                    }
                    state.isActive = false
                }
                activeGesture = null
                Log.d(TAG, "Cleared active gesture: ${switchEvent.code}")
            } else {
                Log.d(TAG, "Ignored completion of ${gesture.id} - not the active gesture")
            }
        }
    }

    private fun findSwitchEventForGesture(gesture: CameraSwitchFacialGesture): SwitchEvent? =
        SwitchEventProvider.findCamera(gesture.id)

    companion object {
        private const val TAG = "CameraSwitchManager"
        private const val SMILE_THRESHOLD = 0.5f
        private const val EYE_OPEN_THRESHOLD = 0.2f
        private const val MIN_FACE_SIZE = 0.2f
    }
}