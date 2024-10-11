package com.example.medhay

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.medhay.databinding.FragmentProfileBinding
import com.example.medhay.databinding.DialogUpdatePasswordBinding
import com.example.medhay.databinding.DialogUpdateNameBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso  // Picasso for loading images

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        if (user == null) {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show()
            return null
        }

        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(user.uid) // Reference to user data in Firebase Database

        // Set initial name and profile picture
        loadProfileImage()
        fetchAndDisplayUserName()  // Fetch name from Firebase Realtime Database

        // Set up listeners using binding
        binding.profilepic.setOnClickListener {
            pickImageFromGallery()
        }

        binding.editname.setOnClickListener {
            showUpdateNameDialog()
        }

        binding.changepassword.setOnClickListener {
            showUpdatePasswordDialog()
        }

        return binding.root
    }

    private fun fetchAndDisplayUserName() {
        // Fetch the user's name from Firebase Realtime Database
        databaseReference.child("name").get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java)
            updateDisplayedName(name)
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to load name: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDisplayedName(name: String?) {
        // Update the TextView that displays the name below the profile picture
        if (name.isNullOrEmpty()) {
            binding.userNameTextView.text = "Anonymous"
        } else {
            binding.userNameTextView.text = name
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.circularImageView.setImageURI(imageUri)
            uploadImageToFirebase()
        } else {
            Toast.makeText(context, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val fileRef = storageReference.child("${user.uid}.jpg")
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build()

                        // Update Firebase Auth user profile
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(context, "Profile Picture Updated", Toast.LENGTH_SHORT).show()

                                // Store the image URL in Firebase Realtime Database under "profileImageUrl"
                                databaseReference.child("profileImageUrl").setValue(uri.toString())
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            loadProfileImage()  // Load the updated image immediately
                                            Toast.makeText(context, "Profile Image URL saved to Database", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to save image URL to Database: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage() {
        // Check if the user has a profile image URL and load it using Picasso
        val photoUri = user.photoUrl
        if (photoUri != null) {
            Picasso.get().load(photoUri).into(binding.circularImageView)
        } else {
            // Load a default image if the user has no profile image
            binding.circularImageView.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun showUpdatePasswordDialog() {
        // Use ViewBinding for the password dialog
        val dialogBinding = DialogUpdatePasswordBinding.inflate(LayoutInflater.from(requireContext()))

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val oldPassword = dialogBinding.oldpasslog.text.toString().trim()
                val newPassword = dialogBinding.newpasslog.text.toString().trim()
                if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    updatePassword(oldPassword, newPassword)
                } else {
                    Toast.makeText(context, "Please enter both old and new passwords", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        dialogBuilder.create().show()
    }

    private fun reauthenticateUser(oldPassword: String, callback: (Boolean) -> Unit) {
        val email = user.email
        val credential = EmailAuthProvider.getCredential(email!!, oldPassword)

        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true)
            } else {
                Toast.makeText(context, "Re-authentication failed", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
    }

    private fun updatePassword(oldPassword: String, newPassword: String) {
        reauthenticateUser(oldPassword) { success ->
            if (success) {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Password Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showUpdateNameDialog() {
        // Use ViewBinding for the name dialog
        val dialogBinding = DialogUpdateNameBinding.inflate(LayoutInflater.from(requireContext()))

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val newName = dialogBinding.editNewName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateName(newName)
                } else {
                    Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        dialogBuilder.create().show()
    }

    private fun updateName(newName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Name Updated", Toast.LENGTH_SHORT).show()
                updateDisplayedName(newName)  // Update the displayed name immediately

                // Save the updated name to Firebase Realtime Database
                databaseReference.child("name").setValue(newName)
            } else {
                Toast.makeText(context, "Failed to update name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
