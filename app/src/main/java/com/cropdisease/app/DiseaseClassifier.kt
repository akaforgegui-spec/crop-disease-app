package com.cropdisease.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private var classifier: DiseaseClassifier? = null
    private var hasCameraPermission by mutableStateOf(false)
    private var startupError by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // CRASH PROTECTOR: If the model is missing, catch the error instead of crashing
        try {
            classifier = DiseaseClassifier(this)
        } catch (e: Exception) {
            startupError = "AI Model Error: Make sure crop_disease_model.onnx is inside app/src/main/assets/. Details: ${e.message}"
        }

        // Check Camera Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (startupError != null) {
                            Text(
                                text = startupError!!, 
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else if (hasCameraPermission) {
                            Text(text = "App and AI initialized successfully!", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { /* Camera code goes here next */ }) {
                                Text("Ready to Scan")
                            }
                        } else {
                            Text("Camera permission is required.")
                        }
                    }
                }
            }
        }
    }
}

