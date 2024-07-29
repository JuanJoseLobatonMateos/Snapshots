package com.jlobatonm.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.jlobatonm.snapshots.databinding.FragmentHomeBinding
import com.jlobatonm.snapshots.databinding.ItemSnapshotBinding


class HomeFragment : Fragment()
{
    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot , SnapshotHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    
    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View
    {
        mBinding = FragmentHomeBinding.inflate(inflater , container , false)
        // Inflate the layout for this fragment
        return mBinding.root
    }
    
    override fun onViewCreated(view: View , savedInstanceState: Bundle?)
    {
        super.onViewCreated(view , savedInstanceState)
        val query = FirebaseDatabase.getInstance(getString(R.string.database_connection)).reference.child("snapshots")
        val options = FirebaseRecyclerOptions.Builder<Snapshot>()
            .setQuery(query , Snapshot::class.java)
            .build()
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot , SnapshotHolder>(options)
        {
            private lateinit var mContext: Context
            override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): SnapshotHolder
            {
                mContext = parent.context
                
                val itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_snapshot , parent , false)
                
                return SnapshotHolder(itemView)
            }
            
            override fun onBindViewHolder(holder: SnapshotHolder , position: Int , model: Snapshot)
            {
                val snapshot = getItem(position)
                with(holder)
                {
                    setListener(snapshot)
                    
                    binding.tvTitle.text = snapshot.title
                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgSnapshot)
                }
            }
            
            @SuppressLint("NotifyDataSetChanged")//eror interno firebase ui  8.0.0
            override fun onDataChanged()
            {
                super.onDataChanged()
                mBinding.pbLoading.visibility = View.GONE
                notifyDataSetChanged()
            }
            
            override fun onError(error: DatabaseError)
            {
                super.onError(error)
                Toast.makeText(mContext , error.message , Toast.LENGTH_SHORT).show()
            }
            
        }
        mLayoutManager = LinearLayoutManager(context)
        
        mBinding.rvSnapshots.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }
    
    override fun onStart()
    {
        super.onStart()
        mFirebaseAdapter.startListening()
    }
    
    override fun onStop()
    {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }
    
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view)
    {
     
        val binding = ItemSnapshotBinding.bind(view)
        
        fun setListener(snapshot: Snapshot)
        {
        
        }

    }
}