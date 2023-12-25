package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.thenewsapp.R
import com.example.thenewsapp.databinding.FragmentLoginBinding
import com.example.thenewsapp.ui.NewsViewModel
import com.example.thenewsapp.ui.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    lateinit var newsViewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        newsViewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        setupLoginButton()
        setupRegisterTextView()
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRegisterTextView() {
        binding.registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful, navigate to ProfileFragment or another screen
                    newsViewModel.getHeadlines("us")
                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                } else {
                    // Login failed, show error message
                    Snackbar.make(binding.root, "Login failed: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
