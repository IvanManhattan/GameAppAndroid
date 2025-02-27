package com.ivanl.gameappandroid

import adapter.GameAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.Game

class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        gameAdapter = GameAdapter(mutableListOf()) { game ->
            val intent = Intent(requireContext(), GameDetailActivity::class.java)
            intent.putExtra("id", game.id)
            startActivity(intent)
        }

        recyclerView.adapter = gameAdapter

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadFavorites()

        return view
    }

    private fun loadFavorites() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    if (favorites.isNotEmpty()) {
                        db.collection("games").whereIn("id", favorites).get().addOnSuccessListener { gameDocs ->
                            val games = gameDocs.map { it.toObject(Game::class.java) }
                            gameAdapter.updateGames(games)
                        }
                    }
                }
            }
        }
    }
}
