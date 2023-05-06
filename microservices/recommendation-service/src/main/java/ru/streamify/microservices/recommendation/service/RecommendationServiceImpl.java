package ru.streamify.microservices.recommendation.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.api.core.recommendation.RecommendationService;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.microservices.recommendation.entity.RecommendationEntity;
import ru.streamify.microservices.recommendation.repository.RecommendationRepository;

import static java.util.logging.Level.FINE;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    public Flux<Recommendation> getRecommendations(int songId) {
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        LOG.info("Will get recommendations for song with id={}", songId);

        return repository.findBySongId(songId)
                .log(LOG.getName(), FINE)
                .map(mapper::entityToApi);
    }

    @NotNull
    @Override
    public Mono<Recommendation> createRecommendation(@NotNull Recommendation body) {
        if (body.getSongId() < 1) {
            throw new InvalidInputException("Invalid songId: " + body.getSongId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException(
                                "Duplicate key, Song Id: " + body.getSongId()
                                        + ", Recommendation Id: " + body.getRecommendationId()))
                .map(mapper::entityToApi);
    }

    @NotNull
    @Override
    public Mono<Void> deleteRecommendations(int songId) {
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        LOG.debug("deleteRecommendations: tries to delete recommendations for the song with songId: {}", songId);
        return repository.deleteAll(repository.findBySongId(songId));
    }
}
