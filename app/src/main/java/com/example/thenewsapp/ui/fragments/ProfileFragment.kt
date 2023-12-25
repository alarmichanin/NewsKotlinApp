package com.example.thenewsapp.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.thenewsapp.R
import com.example.thenewsapp.adapters.CategoriesAdapter
import com.example.thenewsapp.databinding.FragmentProfileBinding
import com.example.thenewsapp.models.Category
import com.example.thenewsapp.models.Profile
import com.example.thenewsapp.ui.NewsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var categoriesAdapter: CategoriesAdapter? = null
    lateinit var newsViewModel: NewsViewModel

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newsViewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        firebaseDatabase = FirebaseDatabase.getInstance()
        setupRecyclerView()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            loadUserProfile(currentUser.uid)
        }

        binding.logoutImageView.setOnClickListener {
            logoutUser()
        }

        binding.addCategoryButton.setOnClickListener {
            showCategorySelectionDialog()
        }
    }

    private fun setupRecyclerView() {
        categoriesAdapter = CategoriesAdapter(listOf())
        binding.favouriteCategoriesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = categoriesAdapter
        }
    }

    private fun loadUserProfile(userId: String) {
//        val shimmerLayout = binding.shimmerLayout
//        shimmerLayout.startShimmer()
        val userRef = firebaseDatabase.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            val profile = dataSnapshot.getValue(Profile::class.java)
//            shimmerLayout.stopShimmer()
//            shimmerLayout.visibility = View.VISIBLE
            if (profile != null) {
                binding.profileNameTextView.text = profile.username
                binding.bioTextView.text = trimBio(profile.bio)
                profile.profileImageUrl?.let { imageUrl ->
                    Glide.with(this).load(imageUrl).into(binding.profileImageView)
                }
                val categories = profile.categories ?: listOf()
                updateUIWithSelectedCategories(categories)
            }
        }.addOnFailureListener { exception ->
//            shimmerLayout.stopShimmer()
//            shimmerLayout.visibility = View.GONE
            Toast.makeText(context, "Failed to load profile", Toast.LENGTH_LONG).show()
            Log.e("ProfileFragment", "Error loading profile", exception)
        }
    }

    private fun trimBio(bio: String?): String {
        return if (bio != null && bio.length > 100) "${bio.take(100)}..." else bio ?: ""
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        newsViewModel.getHeadlines("us")
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    private fun showCategorySelectionDialog() {
        val categories = arrayOf("business", "entertainment", "general", "health", "science", "sports", "technology")
        val checkedItems = BooleanArray(categories.size)
        val selectedCategories = mutableListOf<String>()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Categories")
            .setMultiChoiceItems(categories, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedCategories.add(categories[which])
                } else {
                    selectedCategories.remove(categories[which])
                }
            }
            .setPositiveButton("OK") { dialog, _ ->
                saveSelectedCategories(selectedCategories)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveSelectedCategories(selectedCategories: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = firebaseDatabase.getReference("users").child(userId)

        userRef.child("categories").setValue(selectedCategories)
            .addOnSuccessListener {
                newsViewModel.getHeadlines("us")
                updateUIWithSelectedCategories(selectedCategories)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save categories: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUIWithSelectedCategories(selectedCategories: List<String>) {
        val categoriesList = selectedCategories.map { Category(it) }
        categoriesAdapter?.updateCategories(categoriesList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
