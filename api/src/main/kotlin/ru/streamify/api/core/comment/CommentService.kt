package ru.streamify.api.core.comment

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CommentService {

    @GetMapping("/comments")
    fun getComments(@RequestParam(required = true) songId: Int): Flux<Comment>

    fun createComment(body: Comment): Mono<Comment>

    fun deleteComments(songId: Int): Mono<Void>

}