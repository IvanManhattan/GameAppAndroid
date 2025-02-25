package adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivanl.gameappandroid.GameDetailActivity
import com.ivanl.gameappandroid.R
import model.Game

class GameAdapter(
    private var games: MutableList<Game>,
    private val onClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameImage: ImageView = itemView.findViewById(R.id.gameImage)
        val gameTitle: TextView = itemView.findViewById(R.id.gameTitle)
        val gameDescription: TextView = itemView.findViewById(R.id.gameDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_item, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.gameTitle.text = game.name
        holder.gameDescription.text = game.description

        Glide.with(holder.itemView.context)
            .load(game.imageUrl)
            .into(holder.gameImage)

        // Открытие деталей игры по клику
        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, GameDetailActivity::class.java)
            intent.putExtra("id", game.id)  // Передаём gameId
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = games.size

    // Метод для обновления списка игр
    fun updateGames(newGames: List<Game>) {
        games.clear()
        games.addAll(newGames)
        notifyDataSetChanged() // Обновляем RecyclerView
    }



}
