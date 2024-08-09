package com.jlobatonm.snapshots.ui.fragments

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
import com.jlobatonm.snapshots.R
import com.jlobatonm.snapshots.SnapshotsApplication
import com.jlobatonm.snapshots.databinding.FragmentAddBinding
import com.jlobatonm.snapshots.entities.Snapshot
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddFragment : Fragment() {

    // Variables de binding y lanzadores de actividades
    private lateinit var binding: FragmentAddBinding
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    // Referencias a Firebase Storage y Database
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference

    // Variables para la gestión de fotos
    private var photoSelectedUri: Uri? = null
    private var currentPhotoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar referencias a Firebase
        initializeFirebaseReferences()

        // Inicializar lanzadores de actividades
        initializeActivityLaunchers()

        // Configurar listeners para botones
        setupButtonListeners()
    }

    // Inicializar referencias a Firebase Storage y Database
    private fun initializeFirebaseReferences() {
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance(getString(R.string.database_connection))
            .reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
    }

    // Inicializar lanzadores de actividades para galería, cámara y permisos
    private fun initializeActivityLaunchers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = result.data?.data
                binding.imgPhoto.setImageURI(photoSelectedUri)
                binding.tilTitle.visibility = View.VISIBLE
                binding.tvMessage.text = getString(R.string.post_message_valid_title)
                binding.btnSelect.setImageIcon(null)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = Uri.fromFile(currentPhotoPath?.let { File(it) })
                binding.imgPhoto.setImageURI(photoSelectedUri)
                binding.tilTitle.visibility = View.VISIBLE
                binding.tvMessage.text = getString(R.string.post_message_valid_title)
                binding.btnSelect.setImageIcon(null)
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                openGalleryOrCamera()
            } else {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.permission_required)
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .show()
            }
        }
    }

    // Configurar listeners para los botones
    private fun setupButtonListeners() {
        binding.btnPost.setOnClickListener { postSnapshot() }
        binding.btnSelect.setOnClickListener { checkPermissionsAndOpenGalleryOrCamera() }
    }

    // Verificar permisos y abrir galería o cámara
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

    // Mostrar diálogo para seleccionar entre galería y cámara
    private fun openGalleryOrCamera() {
        val options = arrayOf(R.string.select_array_gallery, R.string.select_array_camera)
            .map { getString(it) }
            .toTypedArray()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.select_option_array)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openGallery()
                1 -> openCamera()
            }
        }
        builder.show()
    }

    // Abrir galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Abrir cámara
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("AddFragment", "Error creando archivo: ${ex.message}", ex)
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    SnapshotsApplication.AUTHORITY,
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                try {
                    cameraLauncher.launch(intent)
                } catch (ex: Exception) {
                    Log.e("AddFragment", "Error lanzando cámara: ${ex.message}", ex)
                }
            }
        } else {
            Log.e("AddFragment", "No hay aplicación de cámara disponible")
        }
    }

    // Crear archivo de imagen
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

    // Publicar snapshot
    private fun postSnapshot() {
        hideKeyboard()
        binding.progressBar.visibility = View.VISIBLE
        val key = databaseReference.push().key!!

        val storageRef = storageReference.child(SnapshotsApplication.PATH_SNAPSHOTS)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(key)

        photoSelectedUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnProgressListener {
                    val progress = (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                    binding.progressBar.progress = progress
                    binding.tvMessage.text = getString(R.string.upload_progress, progress)
                }
                .addOnCompleteListener {
                    binding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(binding.root, R.string.post_message_success, Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        val username = FirebaseAuth.getInstance().currentUser!!.displayName
                        saveSnapshot(key, downloadUri.toString(), binding.etTitle.text.toString().trim(), username!!)
                        resetUI()
                    }
                }
                .addOnFailureListener { exception ->
                    handleUploadFailure(exception)
                }
        }
    }

    // Ocultar teclado
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    // Guardar snapshot en la base de datos
    private fun saveSnapshot(key: String, url: String, title: String, userName: String) {
        val snapshot = Snapshot(title = title, photoUrl = url, userName = userName)
        databaseReference.child(key).setValue(snapshot)
    }

    // Manejar fallos en la subida de archivos
    private fun handleUploadFailure(exception: Exception) {
        if (exception is StorageException && exception.errorCode == StorageException.ERROR_NOT_AUTHORIZED) {
            Snackbar.make(binding.root, R.string.post_message_error_not_authorized, Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, R.string.post_message_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    // Reiniciar la interfaz de usuario
    private fun resetUI() {
        binding.tilTitle.visibility = View.GONE
        binding.tvMessage.text = getString(R.string.post_message_title)
        binding.imgPhoto.setImageURI(null)
        photoSelectedUri = null

        // Reiniciar la aplicación
        val intent = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}