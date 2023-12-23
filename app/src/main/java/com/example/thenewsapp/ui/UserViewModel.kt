package com.example.thenewsapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserViewModel(app: Application): AndroidViewModel(app) {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: MutableLiveData<FirebaseUser?> = MutableLiveData()

    init {
        currentUser.value = firebaseAuth.currentUser
    }

    fun register(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser.value = firebaseAuth.currentUser
            } else {
                // Обробка помилок
            }
        }
    }

    fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser.value = firebaseAuth.currentUser
            } else {
                // Обробка помилок
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        currentUser.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
