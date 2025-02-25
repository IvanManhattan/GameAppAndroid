package com.ivanl.gameappandroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import model.Game

class GameDetailActivity : AppCompatActivity() {

    private lateinit var gameImageView: ImageView
    private lateinit var gameTitleTextView: TextView
    private lateinit var gameDescriptionTextView: TextView
    private lateinit var openSteamButton: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Инициализация элементов UI
        gameImageView = findViewById(R.id.gameImageView)
        gameTitleTextView = findViewById(R.id.gameTitleTextView)
        gameDescriptionTextView = findViewById(R.id.gameDescriptionTextView)
        openSteamButton = findViewById(R.id.openSteamButton)

        // Получаем gameId из Intent
        val gameId = intent.getStringExtra("id")

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance()

        // Загружаем детали игры
        gameId?.let { loadGameDetails(gameId) }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Закрываем активность и возвращаемся назад
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Метод для загрузки данных игры
    private fun loadGameDetails(gameId: String) {
        db.collection("games")
            .whereEqualTo("id", gameId) // Ищем по полю "id"
            .limit(1) // Нам нужен только один документ
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0] // Берём первый найденный документ
                    val game = document.toObject(Game::class.java)
                    game?.let {
                        gameTitleTextView.text = it.name
                        gameDescriptionTextView.text = it.description

                        Glide.with(this)
                            .load(it.imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .into(gameImageView)

                        if (!it.steamUrl.isNullOrEmpty()) {
                            openSteamButton.setOnClickListener { _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.steamUrl))
                                startActivity(intent)
                            }
                        } else {
                            openSteamButton.isEnabled = false
                        }
                    }
                } else {
                    Log.e("GameDetail", "Игра с id=$gameId не найдена в базе!")
                    Toast.makeText(this, "Игра не найдена!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("GameDetail", "Ошибка загрузки данных", e)
                Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
    }


}