package com.example.myapplication_prueba

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform