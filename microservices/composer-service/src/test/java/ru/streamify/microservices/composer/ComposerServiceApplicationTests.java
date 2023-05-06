package ru.streamify.microservices.composer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.api.core.song.Song;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.api.exception.NotFoundException;
import ru.streamify.microservices.composer.service.IntegrationService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {TestSecurityConfig.class},
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=test",
                "spring.main.allow-bean-definition-overriding=true"
        }
)
class ComposerServiceApplicationTests {
    private static final int SONG_ID_OK = 1;
    private static final int SONG_ID_NOT_FOUND = 2;
    private static final int SONG_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private IntegrationService integration;

    @BeforeEach
    void setUp() {
        when(integration.getSong(SONG_ID_OK))
                .thenReturn(Mono.just(new Song(SONG_ID_OK, "name", "author", "streamingUrl")));

        when(integration.getComments(SONG_ID_OK))
                .thenReturn(Flux.fromIterable(List.of(new Comment(SONG_ID_INVALID, 1, "author", "content"))));

        when(integration.getRecommendations(SONG_ID_OK))
                .thenReturn(Flux.fromIterable(List.of(new Recommendation(SONG_ID_INVALID, 1, "author", 1, "content"))));

        when(integration.getSong(SONG_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + SONG_ID_NOT_FOUND));

        when(integration.getSong(SONG_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + SONG_ID_INVALID));
    }

    @Test
    void getSongById() {
        getAndVerifySong(SONG_ID_OK, OK)
                .jsonPath("$.songId").isEqualTo(SONG_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.comments.length()").isEqualTo(1);
    }

    @Test
    void getSongNotFound() {
        getAndVerifySong(SONG_ID_NOT_FOUND, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/api/common/" + SONG_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + SONG_ID_NOT_FOUND);
    }

    @Test
    void getSongInvalidInput() {
        getAndVerifySong(SONG_ID_INVALID, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/api/common/" + SONG_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + SONG_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifySong(int songId, HttpStatus expectedStatus) {
        return client.get()
                .uri("/api/common/" + songId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

}
