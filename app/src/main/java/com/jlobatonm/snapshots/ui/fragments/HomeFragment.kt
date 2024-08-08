package com.jlobatonm.snapshots.ui.fragments

import com.jlobatonm.snapshots.ui.FullScreenImageActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.jlobatonm.snapshots.R
import com.jlobatonm.snapshots.SnapshotsApplication
import com.jlobatonm.snapshots.databinding.FragmentHomeBinding
import com.jlobatonm.snapshots.databinding.ItemSnapshotBinding
import com.jlobatonm.snapshots.entities.Snapshot
import com.jlobatonm.snapshots.utils.HomeAux
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), HomeAux {
    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val query = FirebaseDatabase.getInstance(getString(R.string.database_connection)).reference
            .child(SnapshotsApplication.PATH_SNAPSHOTS)
            .orderByChild(SnapshotsApplication.PROPERTY_CREATED_AT)
        val options = FirebaseRecyclerOptions.Builder<Snapshot>()
            .setQuery(query, SnapshotParser {
                val snapshot = it.getValue(Snapshot::class.java)
                snapshot?.id = it.key!!
                snapshot!!
            }).build()
        
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options) {
            private lateinit var mContext: Context
            
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context
                val itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolder(itemView)
            }
            
            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)
                with(holder) {
                    setListener(snapshot)
                    binding.tvTitle.text = snapshot.title
                    binding.cbLike.text = snapshot.likelist.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapshot.likelist.containsKey(it.uid)
                    }
                    val uploadDate = Date(model.timestamp).toFormattedString("dd/MM/yyyy 'a las' HH:mm")
                    binding.tvUser?.text = "Subido por ${model.userName} el $uploadDate"
                    Glide.with(mContext)
                        .load(model.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgSnapshot)
                    
                    // Load uploader's profile image
                    val userProfileUrl = model.userProfileUrl
                   
                    Glide.with(itemView.context) // Ensure the correct context is used
                            .load(userProfileUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .circleCrop()
                            .into(binding.icUser!!)
                    
                    
                    binding.btnDelete.visibility = if (FirebaseAuth.getInstance().currentUser?.displayName == snapshot.userName) View.VISIBLE else View.GONE
                    binding.imgSnapshot.setOnClickListener {
                        val intent = Intent(mContext, FullScreenImageActivity::class.java)
                        intent.putExtra("IMAGE_URL", model.photoUrl)
                        mContext.startActivity(intent)
                    }
                }
            }
         
            
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.pbLoading.visibility = View.GONE
                notifyDataSetChanged()
            }
            
            override fun onError(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        
        mLayoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        mBinding.rvSnapshots.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }
    
   
    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }
    
    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }
    
    private fun deleteSnapshot(snapshot: Snapshot) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.displayName == snapshot.userName) {
            context?.let { ctx ->
                MaterialAlertDialogBuilder(ctx)
                    .setTitle(R.string.home_delete_title)
                    .setMessage(R.string.home_delete_message)
                    .setPositiveButton(R.string.home_delete_positive) { _, _ ->
                        val databaseReference = FirebaseDatabase.getInstance(getString(R.string.database_connection)).reference
                        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.photoUrl)
                        
                        storageReference.delete().addOnSuccessListener {
                            databaseReference.child(SnapshotsApplication.PATH_SNAPSHOTS).child(snapshot.id).removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(ctx, R.string.home_delete_success, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(ctx, R.string.home_delete_error, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(ctx, ctx.getString(R.string.home_delete_error) + ": " + exception.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton(R.string.home_delete_negative) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        } else {
            Toast.makeText(context, R.string.home_delete_not_allowed, Toast.LENGTH_SHORT).show()
        }
    }
    
    
    private fun setLike(snapshot: Snapshot, checked: Boolean) {
        val databaseReference = FirebaseDatabase.getInstance(getString(R.string.database_connection)).reference.child("snapshots")
        if (checked) {
            databaseReference.child(snapshot.id).child("likelist")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(true)
        } else {
            databaseReference.child(snapshot.id).child("likelist")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(null)
        }
    }
    
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSnapshotBinding.bind(view)
        
        fun setListener(snapshot: Snapshot) {
            binding.btnDelete.setOnClickListener { deleteSnapshot(snapshot) }
            binding.cbLike.setOnCheckedChangeListener { _, checked -> setLike(snapshot, checked) }
        }
    }
    
    override fun goToTop() {
        mBinding.rvSnapshots.smoothScrollToPosition(0)
    }
    
    fun Date.toFormattedString(pattern: String = "dd/MM/yyyy 'a las' HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(this)
    }
    
}
