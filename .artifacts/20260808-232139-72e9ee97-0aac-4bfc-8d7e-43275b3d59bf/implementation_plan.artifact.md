# Implementar CRUD de Barberos Completo

Este plan detalla las actualizaciones necesarias para gestionar barberos desde el panel de administración, incluyendo subida de fotos, asignación de contraseñas, gestión de horarios y eliminación permanente.

## Cambios Propuestos

### Capa de Datos (API)

#### [Greeting.kt](file:///C:/Users/isaac/AndroidStudioProjects/EjemploMoviles/shared/src/commonMain/kotlin/com/example/myapplication_prueba/Greeting.kt)

- **Actualizar `createBarber` y `updateBarber`**: Cambiar a peticiones **Multipart** para enviar el objeto `barber` (JSON) y la `image` (ByteArray).
- **Añadir `deleteBarber`**: Implementar `DELETE /admin/barbers/{id}`.
- **Actualizar `updateBarberSchedule`**: Asegurar que use `POST /admin/barbers/{id}/horario` con el JSON `{"config": "..."}`.

```kotlin
suspend fun deleteBarber(id: Int): RegisterResponse {
    return try {
        val response = getClient().delete("$baseUrl/admin/barbers/$id")
        if (response.status.isSuccess()) RegisterResponse(true, "Barbero eliminado")
        else RegisterResponse(false, "Error al eliminar")
    } catch (e: Exception) { RegisterResponse(false, "Error de red") }
}
```

---

### Interfaz de Usuario (UI)

#### [AdminBarberosView.kt](file:///C:/Users/isaac/AndroidStudioProjects/EjemploMoviles/shared/src/commonMain/kotlin/com/example/myapplication_prueba/admin/AdminBarberosView.kt)

- **Mostrar Imagen**: Usar `KamelImage` en la tarjeta para mostrar la foto real del barbero.
- **Botón de Despido**: Añadir un icono de "usuario despedido" (ej: `PersonRemove`) para eliminar permanentemente.
- **Confirmación de Eliminación**: Diálogo de seguridad antes de borrar.

#### [NuevoBarberoView.kt](file:///C:/Users/isaac/AndroidStudioProjects/EjemploMoviles/shared/src/commonMain/kotlin/com/example/myapplication_prueba/admin/NuevoBarberoView.kt)

- **Campo de Contraseña**: Añadir campo para asignar contraseña temporal.
- **Selector de Imagen**: Integrar el selector de **Cámara/Galería** implementado anteriormente.
- **Actualizar Envío**: Pasar `imageBytes` a la nueva función de `Greeting`.

#### [AdminEditarBarberoView.kt](file:///C:/Users/isaac/AndroidStudioProjects/EjemploMoviles/shared/src/commonMain/kotlin/com/example/myapplication_prueba/admin/AdminEditarBarberoView.kt)

- **Actualizar Envío**: Soportar actualización con foto (Multipart).

## Plan de Verificación

### Verificación Manual
1. **Creación**: Registrar un barbero con nombre, teléfono, correo, **contraseña** y **foto de galería**. Verificar que se guarde.
2. **Visualización**: Ver que la foto aparezca correctamente en el listado.
3. **Horario**: Abrir el modal de horario, seleccionar turnos y guardar. Verificar persistencia.
4. **Edición**: Cambiar el nombre o especialidades y guardar.
5. **Eliminación**: Presionar el icono de "despido", confirmar y verificar que desaparezca y las estadísticas se actualicen.
