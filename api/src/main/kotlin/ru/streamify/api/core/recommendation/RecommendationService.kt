package ru.streamify.api.core.recommendation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface RecommendationService {

    @GetMapping("/recommendations")
    fun getRecommendations(@RequestParam songId: Int): Flux<Recommendation>

    fun createRecommendation(body: Recommendation): Mono<Recommendation>

    fun deleteRecommendations(songId: Int): Mono<Void>

}