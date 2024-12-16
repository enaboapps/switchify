package com.enaboapps.switchify.keyboard.prediction

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.enaboapps.switchify.keyboard.KeyType
import com.enaboapps.switchify.keyboard.KeyboardKey
import com.enaboapps.switchify.keyboard.utils.CapsModeHandler

class PredictionView : LinearLayout {

    private lateinit var onPredictionTapped: (KeyType.Prediction) -> Unit

    private var originalPredictions: List<String> = emptyList()
    private var modifiedPredictions: List<String> = emptyList()

    constructor(
        context: Context,
        onPredictionTapped: (KeyType.Prediction) -> Unit
    ) : super(context) {
        this.onPredictionTapped = onPredictionTapped
        orientation = HORIZONTAL
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        orientation = HORIZONTAL
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        orientation = HORIZONTAL
    }

    fun setPredictions(predictions: List<String>) {
        originalPredictions = predictions
        removeAllViews()
        for (prediction in predictions) {
            val predictionKey = KeyboardKey(context).apply {
                setKeyContent(
                    text = prediction,
                    contentDescription = "Prediction: $prediction"
                )
                tapAction = { onPredictionTapped(KeyType.Prediction(prediction)) }
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            }
            addView(predictionKey)
        }
    }

    fun updateCase() {
        modifiedPredictions = originalPredictions.map { prediction ->
            CapsModeHandler.getCapitalizedText(prediction)
        }
        for (i in 0 until childCount) {
            (getChildAt(i) as KeyboardKey).setKeyContent(
                text = modifiedPredictions[i],
                contentDescription = "Prediction: ${modifiedPredictions[i]}"
            )
        }
    }
}