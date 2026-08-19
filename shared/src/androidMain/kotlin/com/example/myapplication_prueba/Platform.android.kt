package com.example.myapplication_prueba

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun showNotification(title: String, message: String) {
        val context = AppContextHolder.context ?: return
        val channelId = "wolf_look_notifications"
        val notificationId = 1001

        // Crear el Canal (necesario para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Wolf-Look Notifications"
            val descriptionText = "Notificaciones de bienvenida y alertas"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usando un icono genérico por ahora
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Mostrar la notificación
        with(NotificationManagerCompat.from(context)) {
            // En versiones modernas de Android, esto podría fallar si no hay permisos
            // Pero el try-catch o la verificación de permisos se maneja usualmente antes
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Manejar falta de permisos si fuera necesario
            }
        }
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()