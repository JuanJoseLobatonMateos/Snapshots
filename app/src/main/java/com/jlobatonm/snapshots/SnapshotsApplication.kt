package com.jlobatonm.snapshots

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class SnapshotsApplication : Application() {
    companion object {
        const val PROPERTY_CREATED_AT = "timestamp"
        const val PATH_SNAPSHOTS = "snapshots"
        const val PROPERTY_LIKE_LIST = "likeList"
        const val AUTHORITY = "com.jlobatonm.snapshots.fileprovider"
        
        lateinit var currentUser: FirebaseUser
    }
}