package com.example.myapplication_prueba.sensor

expect class CameraManager() {
    fun isCameraAvailable(): Boolean
    suspend fun takePhoto(): ByteArray?
    suspend fun pickImage(): ByteArray?
}