package com.cropdisease.app

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import java.nio.FloatBuffer

class DiseaseClassifier(context: Context) {
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    // Temporary placeholder labels
    private val labels = listOf("Healthy", "Early Blight", "Late Blight", "Leaf Spot")

    init {
        val modelBytes = context.assets.open("crop_disease_model.onnx").readBytes()
        session = env.createSession(modelBytes, OrtSession.SessionOptions())
    }

    fun analyzeImage(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val buffer = FloatBuffer.allocate(1 * 3 * 224 * 224)
        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (c in 0..2) {
            for (pixel in intValues) {
                val value = when (c) {
                    0 -> (pixel shr 16 and 0xFF) / 255.0f 
                    1 -> (pixel shr 8 and 0xFF) / 255.0f  
                    else -> (pixel and 0xFF) / 255.0f     
                }
                buffer.put(value)
            }
        }
        buffer.rewind()

        val inputTensor = OnnxTensor.createTensor(env, buffer, longArrayOf(1, 3, 224, 224))
        val result = session.run(mapOf("input" to inputTensor)) 
        
        val output = result[0].value as Array<FloatArray>
        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        
        return if (maxIndex != -1) labels[maxIndex] else "Unknown"
    }
}

