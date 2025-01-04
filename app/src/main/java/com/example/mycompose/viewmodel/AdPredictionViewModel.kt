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

    // Track the predicted labels for frequency calculation
    private val predictedLabels = mutableListOf<Int>()

    // Define the item labels
    private val itemLabels = listOf(
        "BackPack",      // 0
        "ChargerDevice", // 1
        "Glasses",       // 2
        "Hat",           // 3
        "Headphones",    // 4
        "Phone",         // 5
        "Wallet",        // 6
        "Watch"          // 7
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            modelRepository.loadModel()
        }
    }

    fun predictImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val byteBuffer = convertBitmapToByteBuffer(bitmap)
                val result = modelRepository.predict(byteBuffer)

                // Get the predicted label index and confidence
                val predictedLabel = result.argmax()
                val confidence = result[predictedLabel]

                // Add the predicted label to the list for frequency analysis
                predictedLabels.add(predictedLabel)

                // Get the most frequent label
                val mostFrequentLabel = getMostFrequentLabel(predictedLabels)

                // Update the prediction result with the label and confidence
                predictionResult.value = "Most Frequent Label: ${itemLabels[mostFrequentLabel]}, Confidence: ${"%.2f".format(confidence * 100)}%"

                // Log information for debugging
                Log.d("AdPrediction", "ByteBuffer content: ${byteBuffer}")
                Log.d("AdPrediction", "Raw output: ${result.joinToString(", ")}")
                Log.d("AdPrediction", "Prediction result: ${result.joinToString(", ")}")
                Log.d("AdPrediction", "Predicted label: ${itemLabels[predictedLabel]} (Index: $predictedLabel), Confidence: ${"%.2f".format(confidence * 100)}%")
                Log.d("AdPrediction", "Most Frequent Label: ${itemLabels[mostFrequentLabel]}")
            } catch (e: Exception) {
                Log.e("AdPrediction", "Prediction failed: ${e.message}")
            }
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3) // 224x224 size and 3 color channels
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = intValues[pixel++]

                // R, G, B values
                byteBuffer.putFloat((value shr 16 and 0xFF).toFloat()) // Red
                byteBuffer.putFloat((value shr 8 and 0xFF).toFloat())  // Green
                byteBuffer.putFloat((value and 0xFF).toFloat())        // Blue
            }
        }

        return byteBuffer
    }

    private fun FloatArray.argmax(): Int {
        var maxIndex = 0
        var maxValue = this[0]

        for (i in 1 until this.size) {
            if (this[i] > maxValue) {
                maxValue = this[i]
                maxIndex = i
            }
        }
        return maxIndex
    }

    // Function to calculate the most frequent label in the predictions
    private fun getMostFrequentLabel(labels: List<Int>): Int {
        return labels.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: -1
    }

    override fun onCleared() {
        super.onCleared()
        modelRepository.close()
    }
}
