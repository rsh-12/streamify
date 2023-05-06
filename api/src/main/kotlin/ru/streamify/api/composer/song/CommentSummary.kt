package ru.streamify.api.composer.song

data class CommentSummary(
    val commentId: Int = 0,
    val author: String = "",
    val content: String = ""
)
