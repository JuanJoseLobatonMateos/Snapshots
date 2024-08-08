package com.jlobatonm.snapshots.entities

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/*********************************************************
 * Proyecto : Snapshots
 * From : com.jlobatonm.snapshots
 * Creado por Juan Lobatón en 27/7/2024 a las 23:53
 * Más info:  https://www.linkedin.com/in/jjlobatonmateos
 *            https://github.com/JuanJoseLobatonMateos
 * Todos los derechos reservados 2024
 **********************************************************/

@IgnoreExtraProperties
data class Snapshot(
    @get:Exclude var id: String = "", // Identificador único para el snapshot
    var title: String = "", // Título del snapshot
    var photoUrl: String = "", // URL de la foto
    var likelist: Map<String, Boolean> = mutableMapOf(), // Lista de "me gusta" con IDs de usuarios
    var userName: String = "", // Nombre del usuario que subió el snapshot
    var timestamp: Long = System.currentTimeMillis(), // Marca de tiempo de cuando se creó el snapshot
    var userProfileUrl: String = FirebaseAuth.getInstance().currentUser?.photoUrl.toString() // URL de la foto de perfil del usuario
)