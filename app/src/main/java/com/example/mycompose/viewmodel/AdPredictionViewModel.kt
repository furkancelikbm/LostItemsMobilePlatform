package com.example.mycompose.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycompose.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

@HiltViewModel
class AdPredictionViewModel @Inject constructor(private val modelRepository: ModelRepository) : ViewModel() {

    val predictionResult = mutableStateOf<String>("")

    // Predicted labels for frequency calculation
    private val predictedLabels = mutableListOf<Int>()

    // Item labels corresponding to model output indices
    private val itemLabels = listOf(
        "BackPack", "ChargerDevice", "Glasses", "Hat",
        "Headphones", "Phone", "Wallet", "Watch"
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            modelRepository.loadModel()
        }
    }

    fun predictImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Reset the predicted labels list to avoid old values affecting new predictions
                predictedLabels.clear()

                val byteBuffer = bitmap.toByteBuffer()
                val result = modelRepository.predict(byteBuffer)

                val predictedLabel = result.argmax()
                val confidence = result[predictedLabel]

                predictedLabels.add(predictedLabel)
                val mostFrequentLabel = predictedLabels.mostFrequent()

                predictionResult.value = buildString {
                    append("Most Frequent Label: ${itemLabels[mostFrequentLabel]}")
                    append(", Confidence: ${"%.2f".format(confidence * 100)}%")
                }

                logDebugInfo(byteBuffer, result, predictedLabel, confidence, mostFrequentLabel)
            } catch (e: Exception) {
                Log.e("AdPrediction", "Prediction failed: ${e.message}")
            }
        }
    }


    private fun Bitmap.toByteBuffer(): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(this, 224, 224, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (value in intValues) {
            byteBuffer.putFloat((value shr 16 and 0xFF) / 255.0f) // Red
            byteBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)  // Green
            byteBuffer.putFloat((value and 0xFF) / 255.0f)        // Blue
        }

        return byteBuffer
    }

    private fun FloatArray.argmax(): Int {
        return indices.maxByOrNull { this[it] } ?: -1
    }

    private fun List<Int>.mostFrequent(): Int {
        return groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: -1
    }

    private fun logDebugInfo(
        byteBuffer: ByteBuffer,
        result: FloatArray,
        predictedLabel: Int,
        confidence: Float,
        mostFrequentLabel: Int
    ) {
        Log.d("AdPrediction", "ByteBuffer content: $byteBuffer")
        Log.d("AdPrediction", "Raw output: ${result.joinToString(", ")}")
        Log.d("AdPrediction", "Predicted label: ${itemLabels[predictedLabel]} (Index: $predictedLabel), Confidence: ${"%.2f".format(confidence * 100)}%")
        Log.d("AdPrediction", "Most Frequent Label: ${itemLabels[mostFrequentLabel]}")
    }

    override fun onCleared() {
        super.onCleared()
        modelRepository.close()
    }
}
