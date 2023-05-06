package ru.streamify.api.composer.song

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

// TODO: Add OpenAPI documentation
@RequestMapping("/api/common")
interface SongCompositeService {

    @PostMapping(consumes = ["application/json"])
    fun createSong(@RequestBody body: SongAggregate): Mono<Void>

    @GetMapping("/{songId}", produces = ["application/json"])
    fun getSong(@PathVariable songId: Int): Mono<SongAggregate>

    @DeleteMapping("/{songId}")
    fun deleteSong(@PathVariable songId: Int): Mono<Void>
}