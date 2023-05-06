package ru.streamify.api.core.recommendation

data class Recommendation(
    val songId: Int,
    val recommendationId: Int,
    val author: String,
    val rate: Int,
    var content: String
)
