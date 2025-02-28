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

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

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
                val name = document.getString("name") ?: "Без имени"
                val bio = document.getString("bio") ?: "Нет описания"
                val favorites = document.get("favorites") as? List<String> ?: emptyList()

                val email = auth.currentUser?.email ?: "Unknown Email"

                emailTextView.text = "Email: $email"
                nameTextView.text = "Name: $name"
                bioTextView.text = "Bio: $bio"

                if (favorites.isEmpty()) {
                    favoritesTextView.text = "Favorites: Нет избранных игр"
                } else {
                    val gameNames = mutableListOf<String>()

                    for (gameId in favorites) {
                        db.collection("games").whereEqualTo("id", gameId).limit(1).get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val gameDoc = querySnapshot.documents[0]
                                    val gameName = gameDoc.getString("name") ?: "Неизвестная игра"
                                    gameNames.add(gameName)
                                } else {
                                    gameNames.add("Неизвестная игра")
                                }


                                if (gameNames.size == favorites.size) {
                                    favoritesTextView.text = "Favorites: ${gameNames.joinToString(", ")}"
                                }
                            }
                    }
                }
            }
        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
