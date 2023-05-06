package ru.streamify.microservices.song;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;
import ru.streamify.microservices.song.entity.SongEntity;
import ru.streamify.microservices.song.repository.SongRepository;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private SongRepository repository;

    private SongEntity savedEntity;

    @BeforeEach
    void setUp() {
        StepVerifier
                .create(repository.deleteAll())
                .verifyComplete();

        SongEntity entity = new SongEntity(1, "n", "a", "s");
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areSongEqual(entity, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    void createSong() {
        SongEntity newEntity = new SongEntity(2, "n", "a", "s");

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getSongId().equals(createdEntity.getSongId()))
                .verifyComplete();

        StepVerifier.create(repository.findBySongId(newEntity.getSongId()))
                .expectNextMatches(foundEntity -> areSongEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void updateSong() {
        savedEntity.setName("n2");

        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findBySongId(savedEntity.getSongId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    @Test
    void deleteSong() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getSongById() {
        StepVerifier.create(repository.findBySongId(savedEntity.getSongId()))
                .expectNextMatches(foundEntity -> areSongEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    void duplicateError() {
        SongEntity entity = new SongEntity(savedEntity.getSongId(), "n", "a", "s");
        StepVerifier.create(repository.save(entity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void optimisticLock() {
        SongEntity entity1 = repository.findBySongId(savedEntity.getSongId()).block();
        SongEntity entity2 = repository.findBySongId(savedEntity.getSongId()).block();

        entity1.setName("n2");
        repository.save(entity1).block();

        StepVerifier.create(repository.save(entity2))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        StepVerifier.create(repository.findBySongId(savedEntity.getSongId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion().equals(1) && foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    private boolean areSongEqual(SongEntity expected, SongEntity actual) {
        return (expected.getId().equals(actual.getId())
                && (expected.getVersion().equals(actual.getVersion()))
                && (expected.getSongId().equals(actual.getSongId()))
                && (expected.getName().equals(actual.getName()))
                && (expected.getAuthor().equals(actual.getAuthor()))
                && (expected.getStreamingUrl().equals(actual.getStreamingUrl())));
    }
}
