package ru.streamify.api.core.song

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import reactor.core.publisher.Mono

interface SongService {

    @GetMapping("/songs/{songId}")
    fun getSong(@PathVariable songId: Int): Mono<Song>

    fun createSong(body: Song): Mono<Song>

    fun deleteSong(songId: Int): Mono<Void>

}