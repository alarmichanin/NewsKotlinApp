package com.example.thenewsapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.thenewsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserViewModel(app: Application): AndroidViewModel(app) {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData для відстеження користувача
    val currentUser: MutableLiveData<FirebaseUser?> = MutableLiveData()

    init {
        // Ініціалізація поточного користувача
        currentUser.value = firebaseAuth.currentUser
    }

    fun register(email: String, password: String) {
        // Реєстрація нового користувача
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser.value = firebaseAuth.currentUser

                } else {
                    // Обробка помилок
                }
            }
    }

    fun login(email: String, password: String) {
        // Вхід користувача
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser.value = firebaseAuth.currentUser
                    this.setupLoggedInUI()
                } else {
                    // Обробка помилок
                }
            }
    }

    fun logout() {
        // Вихід користувача
        firebaseAuth.signOut()
        currentUser.value = null
        this.setupLoginUI()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun setupLoggedInUI() {
        val navController = findNavController(R.id.newsNavHostFragment)
        navController.navigate(R.id.profileFragment)
    }

    private fun setupLoginUI() {
        val navController = findNavController(R.id.newsNavHostFragment)
        navController.navigate(R.id.loginFragment)
    }
}
