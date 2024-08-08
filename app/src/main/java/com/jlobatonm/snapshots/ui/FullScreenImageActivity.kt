// Step 2: Update FullScreenImageActivity to use view binding

package com.jlobatonm.snapshots.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jlobatonm.snapshots.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("IMAGE_URL")

        Glide.with(this)
            .load(imageUrl)
            .into(binding.fullScreenImageView)

        binding.fullScreenImageView.setOnClickListener {
            finish()
        }
    }
}