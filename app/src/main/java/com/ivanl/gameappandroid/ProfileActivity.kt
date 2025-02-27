package com.ivanl.gameappandroid

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var favoritesTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        emailTextView = findViewById(R.id.textViewEmail)
        nameTextView = findViewById(R.id.textViewName)
        bioTextView = findViewById(R.id.textViewBio)
        favoritesTextView = findViewById(R.id.textViewFavorites)
        editProfileButton = findViewById(R.id.buttonEditProfile)
        logoutButton = findViewById(R.id.profileButtonLogout)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Показываем стрелку "назад"

        val user = auth.currentUser
        if (user != null) {
            loadUserProfile(user.uid)
        }

        // Переход в редактирование профиля
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Выход из аккаунта
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val user = auth.currentUser
        if (user != null) {
            loadUserProfile(user.uid)
        }
    }

    private fun loadUserProfile(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {

                val name = document.getString("name")
                val bio = document.getString("bio")
                val favorites = document.get("favorites") as? List<String> ?: emptyList()

                val email = auth.currentUser?.email ?: "Unknown Email"

                emailTextView.text = "Email: $email"
                nameTextView.text = "Name: $name"
                bioTextView.text = "Bio: $bio"
                favoritesTextView.text = "Favorites: ${favorites.joinToString(", ")}"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // Обработка нажатия на стрелку "назад"
                finish() // Просто закрываем активити, возвращаясь назад
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
