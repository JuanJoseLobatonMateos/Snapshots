package com.jlobatonm.snapshots.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jlobatonm.snapshots.SnapshotsApplication
import com.jlobatonm.snapshots.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    // Variable de binding para el layout de la actividad
    private lateinit var binding: ActivityFullScreenImageBinding

    // Método onCreate para inicializar la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inflar el layout usando view binding
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la URL de la imagen desde el intent
        val imageUrl = intent.getStringExtra(SnapshotsApplication.PATH_USER_IMAGES)

        // Cargar la imagen usando Glide
        Glide.with(this)
            .load(imageUrl)
            .into(binding.fullScreenImageView)

        // Configurar el listener para cerrar la actividad al hacer clic en la imagen
        binding.fullScreenImageView.setOnClickListener {
            finish()
        }
    }
}