package ru.streamify.microservices.comment.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.streamify.microservices.comment.entity.CommentEntity;

public interface CommentRepository extends ReactiveMongoRepository<CommentEntity, String> {

    Flux<CommentEntity> findBySongId(Integer songId);

}
