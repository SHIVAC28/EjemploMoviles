package com.example.myapplication_prueba

fun Double.format(digits: Int): String {
    val s = this.toString()
    if (!s.contains(".")) return "$s." + "0".repeat(digits)
    val parts = s.split(".")
    val decimal = parts[1].padEnd(digits, '0').take(digits)
    return "${parts[0]}.$decimal"
}
