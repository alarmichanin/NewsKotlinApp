package com.example.thenewsapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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

                } else {
                    // Обробка помилок
                }
            }
    }

    fun logout() {
        // Вихід користувача
        firebaseAuth.signOut()
        currentUser.value = null
    }

    fun isUserLoggedIn(): Boolean {
        // Перевірка залогінений користувач чи ні
        return firebaseAuth.currentUser != null
    }
}
