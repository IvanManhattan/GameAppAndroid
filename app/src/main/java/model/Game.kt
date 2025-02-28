package model

data class Game(
    val id: String = "",
    val name: String = "",
    val imageUrls: List<String> = emptyList(),
    val description: String = "",
    val steamUrl: String = "",
    val releaseDate: String = "",
    val genres: List<String> = emptyList(),

)