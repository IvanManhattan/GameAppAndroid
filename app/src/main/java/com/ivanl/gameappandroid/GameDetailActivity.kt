package com.ivanl.gameappandroid

import adapter.GameImageSliderAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.Game

class GameDetailActivity : AppCompatActivity() {

    private lateinit var gameImageSlider: ViewPager2
    private lateinit var gameTitleTextView: TextView
    private lateinit var gameDescriptionTextView: TextView
    private lateinit var gameReleaseDateTextView: TextView
    private lateinit var gameGenresTextView: TextView
    private lateinit var openSteamButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var favoriteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gameImageSlider = findViewById(R.id.gameImageSlider)
        gameTitleTextView = findViewById(R.id.gameTitleTextView)
        gameDescriptionTextView = findViewById(R.id.gameDescriptionTextView)
        gameReleaseDateTextView = findViewById(R.id.gameReleaseDateTextView)
        gameGenresTextView = findViewById(R.id.gameGenresTextView)
        openSteamButton = findViewById(R.id.openSteamButton)
        favoriteButton = findViewById(R.id.favoriteButton)

        val gameId = intent.getStringExtra("id")

        db = FirebaseFirestore.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            favoriteButton.visibility = View.GONE
        } else {
            gameId?.let { updateFavoriteButton(it) }

            favoriteButton.setOnClickListener {
                gameId?.let { gameId -> toggleFavorite(gameId) }
            }
        }

        gameId?.let { loadGameDetails(it) }
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

    private fun loadGameDetails(gameId: String) {
        db.collection("games")
            .whereEqualTo("id", gameId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val game = document.toObject(Game::class.java)
                    game?.let {
                        gameTitleTextView.text = it.name
                        gameDescriptionTextView.text = it.description
                        gameReleaseDateTextView.text = "Дата выхода: ${it.releaseDate}"
                        gameGenresTextView.text = "Жанры: ${it.genres.joinToString(", ")}"

                        if (it.imageUrls.isNotEmpty()) {
                            val adapter = GameImageSliderAdapter(it.imageUrls)
                            gameImageSlider.adapter = adapter
                        }

                        if (!it.steamUrl.isNullOrEmpty()) {
                            openSteamButton.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(game.steamUrl))
                                startActivity(intent)
                            }
                        } else {
                            openSteamButton.isEnabled = false
                        }
                    }
                } else {
                    Log.e("GameDetail", "Игра с id=$gameId не найдена!")
                    Toast.makeText(this, "Игра не найдена!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("GameDetail", "Ошибка загрузки данных", e)
                Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleFavorite(gameId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val favorites = document.get("favorites") as? MutableList<String> ?: mutableListOf()
                    val isFavorite = favorites.contains(gameId)

                    if (isFavorite) {
                        favorites.remove(gameId)
                    } else {
                        favorites.add(gameId)
                    }

                    userRef.update("favorites", favorites).addOnSuccessListener {
                        favoriteButton.text = if (isFavorite) "Добавить в избранное" else "Удалить из избранного"

                        val message = if (isFavorite) "Удалено из избранного" else "Добавлено в избранное"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun updateFavoriteButton(gameId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    val isFavorite = favorites.contains(gameId)

                    favoriteButton.text = if (isFavorite) "Удалить из избранного" else "Добавить в избранное"
                }
            }
        }
    }


}

