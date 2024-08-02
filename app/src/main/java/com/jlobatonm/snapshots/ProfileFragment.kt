package com.jlobatonm.snapshots

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
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
    
    public fun devolverUsuario(): String
    {
        return mBinding.tvName.toString()
    }
}