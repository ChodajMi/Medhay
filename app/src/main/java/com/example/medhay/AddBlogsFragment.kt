package com.example.medhay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.medhay.databinding.FragmentAddBlogsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddBlogsFragment : Fragment() {

    // View Binding
    private var _binding: FragmentAddBlogsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null
    private var name: String? = null
    private var email: String? = null
    private var dp: String? = null

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentAddBlogsBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Initialize image picker and permission launcher
        initializeImagePickers()
        initializePermissionLauncher()

        // Retrieving user data
        retrieveUserData()

        // Set up click listeners
        binding.imagep.setOnClickListener { pickFromGallery() } // Directly pick from gallery
        binding.pupload.setOnClickListener { uploadBlog() }

        return binding.root
    }

    private fun initializeImagePickers() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                imageUri = result.data?.data
                binding.imagep.setImageURI(imageUri)
            }
        }
    }

    private fun initializePermissionLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val storagePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

            if (storagePermissionGranted) {
                pickFromGallery() // Call this if permissions are granted
            } else {
                Toast.makeText(requireContext(), "Storage permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieveUserData() {
        val userEmail = firebaseAuth.currentUser?.email ?: return
        val query = databaseReference.orderByChild("email").equalTo(userEmail)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshot1 in dataSnapshot.children) {
                    name = dataSnapshot1.child("name").value.toString()
                    email = dataSnapshot1.child("email").value.toString()
                    dp = dataSnapshot1.child("image").value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun pickFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(galleryIntent)
    }

    private fun checkStoragePermission(): Boolean {
        val storagePermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        permissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun uploadBlog() {
        val title = binding.ptitle.text.toString().trim()
        val description = binding.pdes.text.toString().trim()

        // Input validation
        if (TextUtils.isEmpty(title)) {
            binding.ptitle.error = "Title can't be empty"
            return
        }
        if (TextUtils.isEmpty(description)) {
            binding.pdes.error = "Description can't be empty"
            return
        }
        if (imageUri == null) {
            Toast.makeText(requireContext(), "Select an Image", Toast.LENGTH_LONG).show()
            return
        }

        // Show progress
        val progressBar = binding.progressBar // Ensure you have a ProgressBar in your layout
        progressBar.visibility = View.VISIBLE

        val timestamp = System.currentTimeMillis().toString()
        val filepathname = "Posts/post$timestamp"
        val bitmap = (binding.imagep.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference.child(filepathname)
        storageReference.putBytes(data).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                val hashMap = HashMap<String, String?>().apply {
                    put("uid", firebaseAuth.currentUser?.uid)
                    put("uname", name)
                    put("uemail", email)
                    put("udp", dp)
                    put("title", title)
                    put("description", description)
                    put("uimage", downloadUri.toString())
                    put("ptime", timestamp)
                    put("plike", "0")
                    put("pcomments", "0")
                }

                FirebaseDatabase.getInstance().getReference("Posts")
                    .child(timestamp).setValue(hashMap)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Published", Toast.LENGTH_LONG).show()
                        clearInputFields()
                        // Check if the fragment is still attached before starting a new activity
                        if (isAdded) {
                            activity?.let {
                                startActivity(Intent(requireContext(), DashboardActivity::class.java))
                                it.finish()
                            }
                        }
                    }.addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Failed to publish", Toast.LENGTH_LONG).show()
                    }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to retrieve download URL", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearInputFields() {
        binding.ptitle.text.clear()
        binding.pdes.text.clear()
        binding.imagep.setImageDrawable(null)
        imageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
