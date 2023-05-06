package ru.streamify.api.core.comment

data class Comment(
    val songId: Int,
    val commentId: Int,
    val author: String,
    val content: String
)
