package com.tecvo.taxi.model

// Data class representing a User in Firebase Realtime Database
data class User(
    val uid: String = "",      // Unique identifier for the user
    val name: String = "",     // User's name
    val email: String = "",    // User's email address
    val phone: String = "",    // User's phone number (optional)
    val role: String = ""      // Role of the user (e.g., "driver" or "passenger")
) {
    // Required empty constructor for Firebase
    constructor() : this("", "", "", "", "")
}