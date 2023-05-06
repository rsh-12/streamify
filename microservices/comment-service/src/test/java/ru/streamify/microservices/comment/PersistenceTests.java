package ru.streamify.microservices.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import ru.streamify.microservices.comment.entity.CommentEntity;
import ru.streamify.microservices.comment.repository.CommentRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private CommentRepository repository;

    private CommentEntity savedEntity;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();

        CommentEntity entity = new CommentEntity(1, 2, "a", "c");
        savedEntity = repository.save(entity).block();
        assertNotNull(savedEntity);

        assertEqualsComment(entity, savedEntity);
    }

    private void assertEqualsComment(CommentEntity expected, CommentEntity actual) {
        assertEquals(expected.getSongId(), actual.getSongId());
        assertEquals(expected.getCommentId(), actual.getCommentId());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getContent(), actual.getContent());
    }

    @Test
    void create() {
        CommentEntity newEntity = new CommentEntity(1, 3, "a", "c");
        repository.save(newEntity).block();

        CommentEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEqualsComment(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();

        CommentEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getBySongId() {
        List<CommentEntity> entityList = repository.findBySongId(savedEntity.getSongId()).collectList().block();

        assertThat(entityList, hasSize(1));
        assertEqualsComment(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            CommentEntity entity = new CommentEntity(1, 2, "a", "c");
            repository.save(entity).block();
        });
    }

    @Test
    void optimisticLockError() {
        CommentEntity entity1 = repository.findById(savedEntity.getId()).block();
        CommentEntity entity2 = repository.findById(savedEntity.getId()).block();

        entity1.setAuthor("a1");
        repository.save(entity1).block();

        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            repository.save(entity2).block();
        });

        CommentEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int) updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

}
