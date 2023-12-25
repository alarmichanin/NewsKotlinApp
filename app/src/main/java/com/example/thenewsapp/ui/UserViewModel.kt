package com.example.thenewsapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserViewModel(app: Application): AndroidViewModel(app) {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val currentUserId: String?

    init {
        currentUser.value = firebaseAuth.currentUser
        currentUserId = firebaseAuth.currentUser?.uid
    }


    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
