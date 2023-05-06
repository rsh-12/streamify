package ru.streamify.api.composer.song

data class SongAggregate(
    val songId: Int = 0,
    val name: String = "",
    val author: String = "",
    val streamingUrl: String = "",
    val comments: List<CommentSummary>? = emptyList(),
    val recommendations: List<RecommendationSummary>? = emptyList()
)