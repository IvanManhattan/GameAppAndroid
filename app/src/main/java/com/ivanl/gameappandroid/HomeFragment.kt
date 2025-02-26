package com.ivanl.gameappandroid

import adapter.GameAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import model.Game

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private val gamesList = mutableListOf<Game>()
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        db = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.gamesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        gameAdapter = GameAdapter(mutableListOf()) { game ->
            val intent = Intent(requireContext(), GameDetailActivity::class.java)
            intent.putExtra("id", game.id)
            startActivity(intent)
        }
        recyclerView.adapter = gameAdapter

        fetchGames()

        return view
    }

    private fun fetchGames() {
        db.collection("games").get()
            .addOnSuccessListener { result ->
                gamesList.clear()
                for (document in result) {
                    val game = document.toObject(Game::class.java)
                    gamesList.add(game)
                }
                gameAdapter.updateGames(gamesList)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
    }

}
