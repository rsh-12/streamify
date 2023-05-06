package ru.streamify.api.core.song

data class Song(
    val songId: Int,
    val name: String,
    val author: String,
    val streamingUrl: String
)
