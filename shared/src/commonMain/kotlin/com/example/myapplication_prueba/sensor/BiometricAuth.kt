package com.example.myapplication_prueba.sensor

expect class BiometricAuth() {
    fun isBiometricAvailable(): Boolean
    suspend fun authenticate(
        title: String,
        subtitle: String,
        description: String
    ): BiometricResult
}

sealed class BiometricResult {
    data object Success : BiometricResult()
    data class Failure(val message: String) : BiometricResult()
    data object Error : BiometricResult()
}