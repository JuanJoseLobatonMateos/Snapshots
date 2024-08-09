package com.jlobatonm.snapshots.utils

import com.google.android.material.snackbar.Snackbar
// Interfaz para mostrar mensajes en la actividad principal
interface MainAux {
    fun showMessage(resId: Int, duration: Int = Snackbar.LENGTH_SHORT)
}