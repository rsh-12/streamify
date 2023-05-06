package ru.streamify.api.composer.song

data class RecommendationSummary(
    val recommendationId: Int = 0,
    val author: String = "",
    val rate: Int = 0,
    var content: String = ""
)
