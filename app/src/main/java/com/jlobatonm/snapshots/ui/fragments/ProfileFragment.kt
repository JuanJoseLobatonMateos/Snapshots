package com.jlobatonm.snapshots.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.jlobatonm.snapshots.R
import com.jlobatonm.snapshots.databinding.FragmentProfileBinding


class ProfileFragment : Fragment()
{
    private lateinit var mBinding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View
    {
        mBinding = FragmentProfileBinding.inflate(inflater , container , false)
        return mBinding.root
    }
    
    override fun onViewCreated(view: View , savedInstanceState: Bundle?)
    {
        super.onViewCreated(view , savedInstanceState)
      
        mBinding.tvName.text = FirebaseAuth.getInstance().currentUser?.displayName
        mBinding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
        loadProfileImage()
        mBinding.btnLogout.setOnClickListener {
            signOut()
        }
    }
    
    private fun signOut()
    {
        context?.let {
            AuthUI.getInstance().signOut(it)
                .addOnCompleteListener {
                    Toast.makeText(context , R.string.profile_logout , Toast.LENGTH_SHORT).show()
                }
        }
        
    }
    
    private fun loadProfileImage() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .circleCrop()
                .into(mBinding.imgProfile)
        }
    }
    
}