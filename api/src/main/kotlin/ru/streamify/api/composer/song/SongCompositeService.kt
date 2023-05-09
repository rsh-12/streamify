package ru.streamify.api.composer.song

import org.springframework.cloud.gateway.webflux.ProxyExchange
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

// TODO: Add OpenAPI documentation
@RequestMapping("/api/common")
interface SongCompositeService {

    @ResponseStatus(ACCEPTED)
    @PostMapping(consumes = ["application/json"])
    fun createSong(@RequestBody body: SongAggregate): Mono<Void>

    @GetMapping("/{songId}", produces = ["application/json"])
    fun getSong(@PathVariable songId: Int): Mono<SongAggregate>

    @ResponseStatus(ACCEPTED)
    @DeleteMapping("/{songId}")
    fun deleteSong(@PathVariable songId: Int): Mono<Void>

    @GetMapping("/stream/{fileName}")
    fun streamMedia(@PathVariable fileName: String, proxy: ProxyExchange<ByteArray>): Mono<ResponseEntity<ByteArray>>

}