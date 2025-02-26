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
import androidx.appcompat.widget.SearchView
import com.google.firebase.firestore.FirebaseFirestore
import model.Game

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var db: FirebaseFirestore
    private var gamesList = mutableListOf<Game>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        db = FirebaseFirestore.getInstance()
        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.gamesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        gameAdapter = GameAdapter(mutableListOf()) { game ->
            val intent = Intent(requireContext(), GameDetailActivity::class.java)
            intent.putExtra("id", game.id)
            startActivity(intent)
        }
        recyclerView.adapter = gameAdapter

        fetchGames()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterGames(newText.orEmpty())
                return true
            }
        })

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
            }
    }

    private fun filterGames(query: String) {
        val filteredList = if (query.isEmpty()) {
            emptyList()
        } else {
            gamesList.filter { it.name.contains(query, ignoreCase = true) }
        }

        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            gameAdapter.updateGames(filteredList)
        }
    }
}
