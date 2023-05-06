package ru.streamify.microservices.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.microservices.comment.repository.CommentRepository;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static ru.streamify.api.event.Event.Type.CREATE;
import static ru.streamify.api.event.Event.Type.DELETE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentServiceTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private CommentRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Comment>> messageProcessor;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();
    }

    @Test
    void getCommentsBySongId() {
        int songId = 1;

        sendCreateCommentEvent(songId, 1);
        sendCreateCommentEvent(songId, 2);
        sendCreateCommentEvent(songId, 3);

        assertEquals(3, repository.count().block());

        getAndVerifyCommentsBySongId(songId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].songId").isEqualTo(songId)
                .jsonPath("$[2].commentId").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int songId = 1;
        int commentId = 1;

        sendCreateCommentEvent(songId, commentId);
        assertEquals(1, repository.count().block());

        InvalidInputException thrown = assertThrows(InvalidInputException.class, () ->
                sendCreateCommentEvent(songId, commentId), "Expected a InvalidInputException");
        assertEquals("Duplicate key, Song Id: 1, Comment Id: 1", thrown.getMessage());

        assertEquals(1, repository.count().block());
    }

    @Test
    void deleteComments() {
        int songId = 1;
        int commentId = 1;

        sendCreateCommentEvent(songId, commentId);
        assertEquals(1, repository.count().block());

        sendDeleteCommentEvent(songId);
        assertEquals(0, repository.count().block());

        sendDeleteCommentEvent(songId);
    }

    @Test
    void getCommentsWithMissingParameter() {
        String songId = "";
        getAndVerifyCommentsBySongId(songId, BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/comments")
                .jsonPath("$.message").isEqualTo("Required query parameter 'songId' is not present.");
    }

    @Test
    void getCommentsWithInvalidParameter() {
        String songId = "strId";

        getAndVerifyCommentsBySongId("?songId=" + songId, BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/comments")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getCommentsNotFound() {
        getAndVerifyCommentsBySongId("?songId=" + 333, OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getCommentsWithInvalidParameterNegativeValue() {
        int songId = -1;

        getAndVerifyCommentsBySongId("?songId=" + songId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/comments")
                .jsonPath("$.message").isEqualTo("Invalid songId: " + songId);
    }

    private WebTestClient.BodyContentSpec getAndVerifyCommentsBySongId(int songId, HttpStatus expectedStatus) {
        return getAndVerifyCommentsBySongId("?songId=" + songId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyCommentsBySongId(String songIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/comments" + songIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateCommentEvent(int songId, int commentId) {
        Comment comment = new Comment(songId, commentId, "Author " + commentId, "Content " + commentId);
        Event<Integer, Comment> event = new Event(CREATE, songId, comment);
        messageProcessor.accept(event);
    }

    private void sendDeleteCommentEvent(int songId) {
        Event<Integer, Comment> event = new Event(DELETE, songId, null);
        messageProcessor.accept(event);
    }
}
