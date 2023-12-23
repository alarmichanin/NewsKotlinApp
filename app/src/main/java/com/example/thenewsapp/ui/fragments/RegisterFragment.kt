package com.example.thenewsapp.ui.fragments

import kotlinx.coroutines.tasks.await
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.thenewsapp.databinding.FragmentRegistrationBinding
import com.example.thenewsapp.models.Profile
import com.google.firebase.auth.FirebaseAuth
import com.example.thenewsapp.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_registration) {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var contentPickerLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrationBinding.bind(view)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        initializeContentPickerLauncher()
        setupUploadImageButtonClickListener()

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val username = binding.usernameEditText.text.toString().trim()
            val bio = binding.bioEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password,username,bio)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()
            }
        }

        binding.registerTextView.setOnClickListener {
            navigateToLoginFragment()
        }

    }

    private fun registerUser(email: String, password: String, username: String, bio: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    // Запуск корутини для завантаження фотографії профілю
                    lifecycleScope.launch {
                        val imgUrl = uploadProfilePhoto(userId)
                        val profile = Profile(userId, username, email, bio, imgUrl)
                        saveUserProfileToDatabase(profile)
                        navigateToProfileFragment()
                    }
                } else {
                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserProfileToDatabase(profile: Profile) {
        firebaseDatabase.reference.child("users").child(profile.id.toString()).setValue(profile)
            .addOnSuccessListener {
                navigateToProfileFragment()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save profile: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToProfileFragment() {
        findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
    }

    private fun navigateToLoginFragment() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }

    @SuppressLint("Recycle")
    private fun initializeContentPickerLauncher() {
        contentPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri ->
                val fileSize = getFileSize(selectedImageUri)
                if (fileSize <= 1024 * 1024) {
                    this.selectedImageUri = selectedImageUri
                } else {
                    Toast.makeText(context, "Image size should be less than 1MB", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun uploadProfilePhoto(userId: String): String? {
        val selectedUri = selectedImageUri ?: run {
            Toast.makeText(context, "No image selected", Toast.LENGTH_LONG).show()
            return null
        }

        return try {
            val inputStream = requireContext().contentResolver.openInputStream(selectedUri)
            val imageRef = firebaseStorage.reference.child("images/users/$userId/profileImage.jpg")

            val uploadTask = inputStream?.let { imageRef.putStream(it).await() }
            uploadTask?.storage?.downloadUrl?.await().toString()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun getFileSize(uri: Uri): Long {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
        cursor?.moveToFirst()
        val size = sizeIndex?.let { cursor.getLong(it) } ?: 0L
        cursor?.close()
        return size
    }

    private fun setupUploadImageButtonClickListener() {
        binding.uploadImageButton.setOnClickListener {
            contentPickerLauncher.launch("image/*")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
