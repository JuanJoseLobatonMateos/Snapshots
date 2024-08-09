package com.jlobatonm.snapshots

import android.app.Application
import com.google.firebase.auth.FirebaseUser
// Clase de aplicación para almacenar constantes y variables globales
class SnapshotsApplication : Application() {
    companion object {
        const val PROPERTY_CREATED_AT = "timestamp"
        const val PATH_SNAPSHOTS = "snapshots"
        const val PROPERTY_LIKE_LIST = "likeList"
        const val AUTHORITY = "com.jlobatonm.snapshots.fileprovider"
        const val PATH_USER_IMAGES = "IMAGE_URL"
        
        lateinit var currentUser: FirebaseUser
    }
}