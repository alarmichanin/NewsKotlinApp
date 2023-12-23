package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.thenewsapp.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thenewsapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Отримання та відображення інформації про користувача
            binding.profileNameTextView.text = currentUser.displayName ?: "No Name"
            // Тут можна додати завантаження даних користувача з Firebase Database
        }

        binding.logoutImageView.setOnClickListener {
            logoutUser()
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

