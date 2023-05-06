package ru.streamify.microservices.recommendation.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.streamify.microservices.recommendation.entity.RecommendationEntity;

public interface RecommendationRepository extends ReactiveMongoRepository<RecommendationEntity, String> {

    Flux<RecommendationEntity> findBySongId(Integer songId);

}
