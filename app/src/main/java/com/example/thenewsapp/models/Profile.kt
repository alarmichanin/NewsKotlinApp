package com.example.thenewsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val username: String,
    val email: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
): Serializable
