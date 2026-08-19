package com.example.myapplication_prueba.sensor

import com.example.myapplication_prueba.AppContextHolder
import com.example.myapplication_prueba.CameraLauncher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class CameraManager {
    actual fun isCameraAvailable(): Boolean {
        val context = AppContextHolder.context ?: return false
        return context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_ANY)
    }

    actual suspend fun takePhoto(): ByteArray? = suspendCancellableCoroutine { continuation ->
        val launcher = AppContextHolder.context as? CameraLauncher
        if (launcher == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        launcher.launchCamera { bytes ->
            continuation.resume(bytes)
        }
    }

    actual suspend fun pickImage(): ByteArray? = suspendCancellableCoroutine { continuation ->
        val launcher = AppContextHolder.context as? CameraLauncher
        if (launcher == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        launcher.launchGallery { bytes ->
            continuation.resume(bytes)
        }
    }
}