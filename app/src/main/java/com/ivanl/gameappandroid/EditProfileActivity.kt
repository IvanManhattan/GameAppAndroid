package com.ivanl.gameappandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var nameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var favoritesEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.editTextName)
        bioEditText = findViewById(R.id.editTextBio)
        favoritesEditText = findViewById(R.id.editTextFavorites)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)

        val user = auth.currentUser
        user?.let {
            // Загружаем текущие данные пользователя
            loadUserProfile(it.uid)
        }

        // Кнопка сохранения изменений
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val bio = bioEditText.text.toString().trim()
            val favorites = favoritesEditText.text.toString().split(",").map { it.trim() }

            val updatedProfile = hashMapOf(
                "name" to name,
                "bio" to bio,
                "favorites" to favorites
            )

            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("users").document(uid)
                    .update(updatedProfile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Кнопка отмены
        cancelButton.setOnClickListener {
            finish() // Просто закрыть экран редактирования
        }
    }

    private fun loadUserProfile(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()

                    nameEditText.setText(name)
                    bioEditText.setText(bio)
                    favoritesEditText.setText(favorites.joinToString(", "))
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
