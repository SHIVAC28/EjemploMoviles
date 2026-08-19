package com.example.myapplication_prueba

import androidx.appcompat.app.AppCompatActivity

interface CameraLauncher {
    fun launchCamera(callback: (ByteArray?) -> Unit)
    fun launchGallery(callback: (ByteArray?) -> Unit)
}

object AppContextHolder {
    var context: AppCompatActivity? = null
}