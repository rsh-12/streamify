package ru.streamify.microservices.composer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.api.core.comment.CommentService;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.api.core.recommendation.RecommendationService;
import ru.streamify.api.core.song.Song;
import ru.streamify.api.core.song.SongService;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.api.exception.NotFoundException;
import ru.streamify.util.HttpErrorInfo;

import java.io.IOException;

import static java.util.logging.Level.FINE;
import static ru.streamify.microservices.composer.util.ServiceConstants.API_COMMENT_SERVICE;
import static ru.streamify.microservices.composer.util.ServiceConstants.API_RECOMMENDATION_SERVICE;
import static ru.streamify.microservices.composer.util.ServiceConstants.API_SONG_SERVICE;
import static ru.streamify.microservices.composer.util.ServiceConstants.BINDING_COMMENTS;
import static ru.streamify.microservices.composer.util.ServiceConstants.BINDING_RECOMMENDATIONS;
import static ru.streamify.microservices.composer.util.ServiceConstants.BINDING_SONGS;

@Component
public class IntegrationService implements SongService, CommentService, RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationService.class);

    private final Scheduler publishEventScheduler;
    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final StreamBridge streamBridge;

    public IntegrationService(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge) {

        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;
    }

    @NotNull
    @Override
    public Flux<Comment> getComments(int songId) {
        String url = API_COMMENT_SERVICE + "/comments?songId=" + songId;

        LOG.debug("Will call the getComments API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve()
                .bodyToFlux(Comment.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @NotNull
    @Override
    public Mono<Comment> createComment(@NotNull Comment body) {
        return Mono.fromCallable(() -> {
                    Event<Integer, Comment> event = new Event<>(Event.Type.CREATE, body.getSongId(), body);
                    sendMessage(BINDING_COMMENTS, event);
                    return body;
                })
                .subscribeOn(publishEventScheduler);
    }

    private void sendMessage(String bindingName, Event event) {
        Message<Event> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();

        streamBridge.send(bindingName, message);
    }

    @NotNull
    @Override
    public Mono<Void> deleteComments(int songId) {
        return Mono.fromRunnable(() -> {
                    Event<Integer, Comment> event = new Event<>(Event.Type.DELETE, songId);
                    sendMessage(BINDING_COMMENTS, event);
                })
                .subscribeOn(publishEventScheduler)
                .then();
    }

    @NotNull
    @Override
    public Flux<Recommendation> getRecommendations(int songId) {
        String url = API_RECOMMENDATION_SERVICE + "/recommendations?songId=" + songId;

        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @NotNull
    @Override
    public Mono<Recommendation> createRecommendation(@NotNull Recommendation body) {
        return Mono.fromCallable(() -> {
                    Event<Integer, Recommendation> event = new Event<>(Event.Type.CREATE, body.getSongId(), body);
                    sendMessage(BINDING_RECOMMENDATIONS, event);
                    return body;
                })
                .subscribeOn(publishEventScheduler);
    }

    @NotNull
    @Override
    public Mono<Void> deleteRecommendations(int songId) {
        return Mono.fromRunnable(() -> {
                    Event<Integer, Recommendation> event = new Event<>(Event.Type.DELETE, songId);
                    sendMessage(BINDING_RECOMMENDATIONS, event);
                })
                .subscribeOn(publishEventScheduler)
                .then();
    }

    @NotNull
    @Override
    @Retry(name = "song")
    @TimeLimiter(name = "song")
    @CircuitBreaker(name = "song", fallbackMethod = "getSongFallbackValue")
    public Mono<Song> getSong(int songId) {
        String url = API_SONG_SERVICE + "/songs/" + songId;

        LOG.debug("Will call the getSong API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(Song.class)
                .log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException wcre)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        switch (HttpStatus.valueOf(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));
            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }
        catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    @NotNull
    @Override
    public Mono<Song> createSong(@NotNull Song body) {
        return Mono.fromCallable(() -> {
                    Event<Integer, Song> event = new Event<>(Event.Type.CREATE, body.getSongId(), body);
                    sendMessage(BINDING_SONGS, event);
                    return body;
                })
                .subscribeOn(publishEventScheduler);
    }

    @NotNull
    @Override
    public Mono<Void> deleteSong(int songId) {
        return Mono.fromRunnable(() -> {
                    Event<Integer, Song> event = new Event<>(Event.Type.DELETE, songId);
                    sendMessage(BINDING_SONGS, event);
                })
                .subscribeOn(publishEventScheduler)
                .then();
    }

    private Mono<Song> getSongFallbackValue(int songId, CallNotPermittedException ex) {
        LOG.warn("Creating a fail-fast fallback song for songId = {} and exception = {} ",
                songId, ex.toString());

        Song fallbackSong = new Song(songId, "Fallback song " + songId, "Fallback author", "Fallback streaming url");

        return Mono.just(fallbackSong);
    }
}
