package com.example.thenewsapp.ui.fragments

import kotlinx.coroutines.tasks.await
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.io.IOException

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
        setupTextWatchers()

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
        val handler = CoroutineExceptionHandler { _, exception ->
            Toast.makeText(requireActivity(), "Error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    // Запуск корутини для завантаження фотографії профілю
                    if (selectedImageUri != null) {
                        // Запуск корутини для завантаження фотографії профілю
                        val handler = CoroutineExceptionHandler { _, exception ->
                            Toast.makeText(requireActivity(), "Error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }

                        lifecycleScope.launch(handler) {
                            try {
                                val imgUrl = uploadProfilePhoto(userId)
                                createAndSaveUserProfile(userId, username, email, bio, imgUrl)
                            } catch (e: Exception) {
                                Toast.makeText(requireActivity(), "Error during image upload: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // Створення профілю без зображення
                        createAndSaveUserProfile(userId, username, email, bio, null)
                    }
                } else {
                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createAndSaveUserProfile(userId: String, username: String, email: String, bio: String, imgUrl: String?) {
        val profile = Profile(userId, username, email, bio, imgUrl)
        saveUserProfileToDatabase(profile)
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
        showLoadingState(false)
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(selectedUri)
                ?: throw IOException("Unable to open input stream for the selected image")
            val imageRef = firebaseStorage.reference.child("images/users/$userId/profileImage.jpg")
            val uploadTask = imageRef.putStream(inputStream).await()
            uploadTask.storage.downloadUrl.await().toString()
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
            showLoadingState(true)
            contentPickerLauncher.launch("image/*")
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.uploadImageButton.isEnabled = !isLoading
        binding.registerButton.isEnabled = !isLoading
        binding.uploadImageButton.text = if (isLoading) "Loading..." else "Upload Image"
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = binding.emailEditText.text.toString().trim()
                val password = binding.passwordEditText.text.toString().trim()
                val username = binding.usernameEditText.text.toString().trim()

                binding.registerButton.isEnabled = email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()
            }
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.usernameEditText.addTextChangedListener(textWatcher)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
