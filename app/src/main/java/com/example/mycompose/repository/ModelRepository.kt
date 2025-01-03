package com.example.mycompose.repository

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.exp

class ModelRepository @Inject constructor(private val context: Context) {

    private lateinit var interpreter: Interpreter

    // Modeli yükleme
    fun loadModel() {
        val assetManager = context.assets
        val modelPath = "mobilenetv1.tflite" // Modelin dosya adı
        val fileDescriptor = assetManager.openFd(modelPath)

        val fileInputStream = fileDescriptor.createInputStream()
        val modelByteArray = fileInputStream.readBytes()
        fileInputStream.close()
        fileDescriptor.close()

        val byteBuffer = ByteBuffer.allocateDirect(modelByteArray.size)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.put(modelByteArray)

        interpreter = Interpreter(byteBuffer)
    }

    // Fotoğraf tahmini yapma
    fun predict(image: ByteBuffer): FloatArray {
        val output = Array(1) { FloatArray(8) } // Çıktı şekli [1, 8]
        interpreter.run(image, output)
        return applySoftmax(output[0]) // Eğer softmax gerekiyorsa
    }

    // Softmax işlemi
    private fun applySoftmax(logits: FloatArray): FloatArray {
        val expValues = logits.map { exp(it.toDouble()) }
        val sum = expValues.sum()
        return expValues.map { (it / sum).toFloat() }.toFloatArray()
    }

    // Interpreter'i serbest bırakma
    fun close() {
        if (::interpreter.isInitialized) {
            interpreter.close()
        }
    }
}
