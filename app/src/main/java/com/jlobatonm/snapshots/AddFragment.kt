package com.jlobatonm.snapshots

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.jlobatonm.snapshots.databinding.FragmentAddBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddFragment : Fragment() {

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    private var mPhotoSelectedUri: Uri? = null
    private var currentPhotoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance(getString(R.string.database_connection)).reference.child(
            PATH_SNAPSHOTS
        )

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mPhotoSelectedUri = result.data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
                mBinding.btnSelect.setImageIcon(null)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mPhotoSelectedUri = Uri.fromFile(currentPhotoPath?.let { File(it) })
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
                mBinding.btnSelect.setImageIcon(null)
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                openGalleryOrCamera()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission Denied")
                    .setMessage("Permissions are required to access photos and camera. Please enable them in the app settings.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        mBinding.btnPost.setOnClickListener { postSnapshot() }
        mBinding.btnSelect.setOnClickListener { checkPermissionsAndOpenGalleryOrCamera() }
    }

    private fun checkPermissionsAndOpenGalleryOrCamera() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            openGalleryOrCamera()
        }
    }

    private fun openGalleryOrCamera() {
        val options = arrayOf("Gallery", "Camera")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openGallery()
                1 -> openCamera()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("AddFragment", "Error creating file: ${ex.message}", ex)
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.jlobatonm.snapshots.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                try {
                    cameraLauncher.launch(intent)
                } catch (ex: Exception) {
                    Log.e("AddFragment", "Error launching camera: ${ex.message}", ex)
                }
            }
        } else {
            Log.e("AddFragment", "No camera app available")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun postSnapshot() {
        hideKeyboard()
        mBinding.progressBar.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!

        val storageReference = mStorageReference.child(PATH_SNAPSHOTS).child(FirebaseAuth.getInstance().currentUser!!.uid).child(key)
        if (mPhotoSelectedUri != null) {
            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                    mBinding.progressBar.progress = progress
                    mBinding.tvMessage.text = getString(R.string.upload_progress, progress)
                }
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener { it ->
                    Snackbar.make(mBinding.root, "Instantanea publicada", Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        val username= FirebaseAuth.getInstance().currentUser?.displayName?:"Usuario desconocido"
                        saveSnapshot(key, it.toString(), mBinding.etTitle.text.toString().trim(), username)
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                        mBinding.imgPhoto.setImageURI(null)
                        mPhotoSelectedUri = null
                    }
                    // Navigate back to HomeFragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment())
                        .commit()
                }
                .addOnFailureListener { exception ->
                    if (exception is StorageException && exception.errorCode == StorageException.ERROR_NOT_AUTHORIZED) {
                        Snackbar.make(mBinding.root, "Error: No permission to access this object", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(mBinding.root, "Error al publicar la instantanea", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mBinding.root.windowToken, 0)
    }
    
    private fun saveSnapshot(key: String, url: String, title: String, userName: String) {
        val snapshot = Snapshot(title = title, photoUrl = url, userName = userName)
        mDatabaseReference.child(key).setValue(snapshot)
    }

    companion object {
        const val PATH_SNAPSHOTS = "snapshots"
    }
}