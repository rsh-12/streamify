package ru.streamify.microservices.song.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import ru.streamify.microservices.song.entity.SongEntity;

public interface SongRepository extends ReactiveMongoRepository<SongEntity, String> {

    Mono<SongEntity> findBySongId(Integer songId);

}
