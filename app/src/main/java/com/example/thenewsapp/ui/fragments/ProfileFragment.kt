package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.thenewsapp.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.thenewsapp.databinding.FragmentProfileBinding
import com.example.thenewsapp.models.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {
    private lateinit var firebaseDatabase: FirebaseDatabase

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseDatabase = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            loadUserProfile(currentUser.uid)
        }

        binding.logoutImageView.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserProfile(userId: String) {
        val userRef = firebaseDatabase.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val profile = dataSnapshot.getValue(Profile::class.java)
            if (profile != null) {
                binding.profileNameTextView.text = profile.username
                binding.bioTextView.text = trimBio(profile.bio)
                // Завантаження зображення профілю, якщо воно існує
                profile.profileImageUrl?.let { imageUrl ->
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.profileImageView)
                }
            }
        }.addOnFailureListener {exception ->
            Toast.makeText(context, "Failed to load profile", Toast.LENGTH_LONG).show()
            Log.e("ProfileFragment", "Error loading profile", exception)
        }
    }

    private fun trimBio(bio: String?): String {
        return if (bio != null && bio.length > 100) {
            "${bio.take(100)}..."
        } else {
            bio ?: ""
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

