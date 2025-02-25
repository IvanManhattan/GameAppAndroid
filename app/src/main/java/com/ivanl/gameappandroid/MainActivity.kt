package com.ivanl.gameappandroid

import adapter.GameAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.Game

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var db: FirebaseFirestore
    private val gamesList = mutableListOf<Game>() // Список для хранения игр

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance()

        // Настройка RecyclerView
        recyclerView = findViewById(R.id.gamesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Загружаем данные
        gameAdapter = GameAdapter(mutableListOf()) { game ->
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("id", game.id)  // Исправил ключ на "id"
            startActivity(intent)
        }
        recyclerView.adapter = gameAdapter  // Устанавливаем пустой адаптер

        fetchGames()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                val user = auth.currentUser
                val intent = if (user != null) {
                    Intent(this, ProfileActivity::class.java) // Если авторизован – открываем профиль
                } else {
                    Intent(this, LoginActivity::class.java) // Если нет – отправляем на логин
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchGames() {
        db.collection("games").get()
            .addOnSuccessListener { result ->
                gamesList.clear()
                for (document in result) {
                    val game = document.toObject(Game::class.java)
                    gamesList.add(game)
                }

                // Проверяем, существует ли уже адаптер
                if (::gameAdapter.isInitialized) {
                    gameAdapter.updateGames(gamesList)  // Обновляем данные в адаптере
                } else {
                    gameAdapter = GameAdapter(gamesList) { game ->
                        val intent = Intent(this, GameDetailActivity::class.java)
                        intent.putExtra("id", game.id)
                        startActivity(intent)
                    }
                    recyclerView.adapter = gameAdapter  // Устанавливаем адаптер только один раз
                }
            }


    }
}
