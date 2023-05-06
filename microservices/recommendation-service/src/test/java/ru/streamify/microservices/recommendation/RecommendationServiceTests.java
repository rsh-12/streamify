package ru.streamify.microservices.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.microservices.recommendation.repository.RecommendationRepository;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationServiceTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private RecommendationRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Recommendation>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void getRecommendationsBySongId() {
        int songId = 1;

        sendCreateRecommendationEvent(songId, 1);
        sendCreateRecommendationEvent(songId, 2);
        sendCreateRecommendationEvent(songId, 3);

        assertEquals(3, repository.findBySongId(songId).count().block());

        getAndVerifyRecommendationsBySongId(songId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].songId").isEqualTo(songId)
                .jsonPath("$[2].recommendationId").isEqualTo(3);
    }

    @Test
    void duplicateError() {
        int songId = 1;
        int recommendationId = 1;

        sendCreateRecommendationEvent(songId, recommendationId);

        assertEquals(1, repository.count().block());

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateRecommendationEvent(songId, recommendationId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Song Id: 1, Recommendation Id: 1", thrown.getMessage());

        assertEquals(1, repository.count().block());
    }

    @Test
    void deleteRecommendations() {
        int songId = 1;
        int recommendationId = 1;

        sendCreateRecommendationEvent(songId, recommendationId);
        assertEquals(1, repository.findBySongId(songId).count().block());

        sendDeleteRecommendationEvent(songId);
        assertEquals(0, repository.findBySongId(songId).count().block());

        sendDeleteRecommendationEvent(songId);
    }

    @Test
    void getRecommendationsMissingParameter() {
        getAndVerifyRecommendationsBySongId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendations")
                .jsonPath("$.message").isEqualTo("Required query parameter 'songId' is not present.");
    }

    @Test
    void getRecommendationsInvalidParameter() {
        getAndVerifyRecommendationsBySongId("?songId=no-int", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendations")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getRecommendationsNotFound() {
        getAndVerifyRecommendationsBySongId("?songId=333", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {
        int songId = -1;

        getAndVerifyRecommendationsBySongId("?songId=" + songId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendations")
                .jsonPath("$.message").isEqualTo("Invalid songId: " + songId);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsBySongId(int songId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendationsBySongId("?songId=" + songId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyRecommendationsBySongId(String songIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/recommendations" + songIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateRecommendationEvent(int songId, int recommendationId) {
        Recommendation recommendation = new Recommendation(
                songId,
                recommendationId,
                "Author " + recommendationId,
                recommendationId,
                "Content " + recommendationId);
        Event<Integer, Recommendation> event = new Event(Event.Type.CREATE, songId, recommendation);
        messageProcessor.accept(event);
    }

    private void sendDeleteRecommendationEvent(int songId) {
        Event<Integer, Recommendation> event = new Event(Event.Type.DELETE, songId, null);
        messageProcessor.accept(event);
    }
}
