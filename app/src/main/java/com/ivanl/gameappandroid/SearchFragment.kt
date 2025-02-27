package com.ivanl.gameappandroid

import adapter.GameAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import model.Game
import androidx.appcompat.widget.SearchView

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var genreSpinner: Spinner
    private lateinit var dateFilterButton: Button
    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter
    private lateinit var db: FirebaseFirestore
    private var selectedGenre: String? = null
    private var selectedYear: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.searchView)
        genreSpinner = view.findViewById(R.id.genreSpinner)
        dateFilterButton = view.findViewById(R.id.dateFilterButton)
        gamesRecyclerView = view.findViewById(R.id.gamesRecyclerView)

        db = FirebaseFirestore.getInstance()
        gameAdapter = GameAdapter(mutableListOf()) {  }
        gamesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        gamesRecyclerView.adapter = gameAdapter

        dateFilterButton.setOnClickListener {
            showYearPicker()
        }

        setupGenreSpinner()
        setupSearch()

        return view
    }

    private fun showYearPicker() {
        val years = (1995..2025).map { it.toString() }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Choose year")
            .setItems(years) { _, which ->
                selectedYear = years[which]
                dateFilterButton.text = "Year: $selectedYear"
                filterGames()
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun setupGenreSpinner() {
        val genres = listOf("All genres", "Action", "RPG", "Strategy", "Shooter", "Adventure", "Family")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genreSpinner.adapter = adapter

        genreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGenre = if (position == 0) null else genres[position]
                filterGames()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterGames()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterGames()
                return true
            }
        })
    }

    private fun filterGames() {
        val queryText = searchView.query.toString().trim()

        var query: Query = db.collection("games")

        if (!selectedGenre.isNullOrEmpty()) {
            query = query.whereArrayContains("genres", selectedGenre!!)
        }

        if (!selectedYear.isNullOrEmpty()) {
            query = query.whereEqualTo("releaseDate", selectedYear!!.toString())
        }

        query.get().addOnSuccessListener { documents ->
            var filteredGames = documents.map { it.toObject(Game::class.java) }

            if (queryText.isNotEmpty()) {
                filteredGames = filteredGames.filter { it.name.contains(queryText, ignoreCase = true) }
            }

            gameAdapter.updateGames(filteredGames)
            gamesRecyclerView.visibility = if (filteredGames.isEmpty()) View.GONE else View.VISIBLE
        }
    }



}
