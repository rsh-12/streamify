package ru.streamify.microservices.song;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.streamify.api.core.song.Song;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.microservices.song.repository.SongRepository;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class SongServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private WebTestClient client;

    @Autowired
    private SongRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Song>> messageProcessor;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();
    }

    @Test
    void getSongById() {
        int songId = 1;

        assertNull(repository.findBySongId(songId).block());
        assertEquals(0, repository.count().block());

        sendCreateSongEvent(songId);

        assertNotNull(repository.findBySongId(songId).block());
        assertEquals(1, repository.count().block());

        getAndVerifySong(songId, HttpStatus.OK)
                .jsonPath("$.songId").isEqualTo(songId);
    }

    private void sendCreateSongEvent(int songId) {
        Song song = new Song(songId, "name", "author", "streamingUrl");
        Event<Integer, Song> event = new Event<>(Event.Type.CREATE, songId, song);
        messageProcessor.accept(event);
    }

    private WebTestClient.BodyContentSpec getAndVerifySong(int songId, HttpStatus expectedStatus) {
        return client.get()
                .uri("/songs/" + songId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void duplicateError() {
        int songId = 1;
        assertNull(repository.findBySongId(songId).block());

        sendCreateSongEvent(songId);

        assertNotNull(repository.findBySongId(songId).block());

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateSongEvent(songId),
                "Expected InvalidInputException"
        );

        assertEquals("Duplicate key, Song Id: " + songId, thrown.getMessage());
    }

    @Test
    void deleteSong() {
        int songId = 1;

        sendCreateSongEvent(songId);
        assertNotNull(repository.findBySongId(songId).block());

        sendDeleteSongEvent(songId);
        assertNull(repository.findBySongId(songId).block());

        sendDeleteSongEvent(songId);
    }

    public void sendDeleteSongEvent(int songId) {
        Event<Integer, Song> event = new Event<>(Event.Type.DELETE, songId);
        messageProcessor.accept(event);
    }

    @Test
    void getSongNotFound() {
        int songId = 333;
        getAndVerifySong(songId, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/songs/" + songId)
                .jsonPath("$.message").isEqualTo("No song found for songId: " + songId);
    }

    @Test
    void getSongInvalidParameterNegativeValue() {
        int songId = -1;
        getAndVerifySong(songId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/songs/" + songId)
                .jsonPath("$.message").isEqualTo("Invalid songId: " + songId);
    }
}
