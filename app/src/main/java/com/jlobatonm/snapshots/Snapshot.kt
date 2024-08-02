package com.jlobatonm.snapshots

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
data class Snapshot(@get:Exclude var id: String = "" , var title: String = "" , var photoUrl: String = "" , var likelist: Map<String, Boolean> = mutableMapOf(), var userName: String = "")
