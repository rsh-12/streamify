package ru.streamify.microservices.composer.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.webflux.ProxyExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.streamify.api.composer.song.CommentSummary;
import ru.streamify.api.composer.song.RecommendationSummary;
import ru.streamify.api.composer.song.SongAggregate;
import ru.streamify.api.composer.song.SongCompositeService;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.api.core.song.Song;
import ru.streamify.microservices.composer.util.ServiceConstants;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static ru.streamify.microservices.composer.util.ServiceConstants.API_STREAMING_SERVICE;

@RestController
public class SongCompositeServiceImpl implements SongCompositeService {
    private static final Logger LOG = LoggerFactory.getLogger(SongCompositeServiceImpl.class);
    private final SecurityContext securityContext = new SecurityContextImpl();
    private final IntegrationService integration;

    @Autowired
    public SongCompositeServiceImpl(IntegrationService integration) {
        this.integration = integration;
    }

    @NotNull
    @Override
    public Mono<Void> createSong(@NotNull SongAggregate body) {
        try {
            return createAggregateSong(body);
        }
        catch (RuntimeException re) {
            LOG.warn("create composite song failed: {}", re.toString());
            throw re;
        }
    }

    @NotNull
    @Override
    public Mono<ResponseEntity<byte[]>> streamMedia(
            @NotNull String fileName,
            @NotNull ProxyExchange<byte[]> proxy) {

        return proxy.uri(API_STREAMING_SERVICE + "/" + fileName).get();
    }

    private Mono<Void> createAggregateSong(SongAggregate body) {
        List<Mono> container = new ArrayList<>();
        container.add(getLogAuthorizationInfoMono());

        LOG.debug("create composite song: creates a new composite entity for songId: {}", body.getSongId());

        Song song = new Song(
                body.getSongId(),
                body.getName(),
                body.getAuthor(),
                body.getStreamingUrl());

        container.add(integration.createSong(song));
        createComments(body, container);
        createRecommendations(body, container);

        LOG.debug("create composite song: composite entities created for songId: {}", body.getSongId());

        return Mono.zip(combiner -> "", container.toArray(new Mono[0]))
                .doOnError(ex -> LOG.warn("create composite song failed: {}", ex.toString()))
                .then();
    }

    private Mono<SecurityContext> getLogAuthorizationInfoMono() {
        return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
    }

    private void createComments(SongAggregate body, List<Mono> container) {
        if (body.getComments() == null) {
            return;
        }

        body.getComments().forEach(c -> {
            Comment comment = new Comment(
                    body.getSongId(),
                    c.getCommentId(),
                    c.getAuthor(),
                    c.getContent());

            container.add(integration.createComment(comment));
        });
    }

    private void createRecommendations(SongAggregate body, List<Mono> container) {
        if (body.getRecommendations() == null) {
            return;
        }

        body.getRecommendations().forEach(r -> {
            Recommendation recommendation = new Recommendation(
                    body.getSongId(),
                    r.getRecommendationId(),
                    r.getAuthor(),
                    r.getRate(),
                    r.getContent());

            container.add(integration.createRecommendation(recommendation));
        });
    }

    private Mono<SecurityContext> getSecurityContextMono() {
        return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(securityContext);
    }

    public void logAuthorizationInfo(SecurityContext securityContext) {
        if (securityContext != null
                && securityContext.getAuthentication() != null
                && securityContext.getAuthentication() instanceof JwtAuthenticationToken jwtAuthToken) {
            Jwt jwtToken = jwtAuthToken.getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            LOG.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            LOG.warn("No JWT supplied, running tests are we?");
        } else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}",
                        subject, scopes, expires, issuer, audience);
            }
        }
    }

    @NotNull
    @Override
    public Mono<SongAggregate> getSong(int songId) {
        LOG.info("Will get composite song for songId={}", songId);

        return Mono.zip(values -> {
                            SecurityContext context = (SecurityContext) values[0];
                            Song song = (Song) values[1];
                            List<Comment> comments = (List<Comment>) values[2];
                            List<Recommendation> recommendations = (List<Recommendation>) values[3];

                            return toAggregateSongDto(context, song, comments, recommendations);
                        },
                        getSecurityContextMono(),
                        integration.getSong(songId),
                        integration.getComments(songId).collectList(),
                        integration.getRecommendations(songId).collectList()
                )
                .doOnError(ex -> LOG.warn("get composite song failed: {}", ex.toString()))
                .log(LOG.getName(), Level.FINE);
    }

    private SongAggregate toAggregateSongDto
            (SecurityContext securityContext,
             Song song,
             List<Comment> comments,
             List<Recommendation> recommendations) {

        logAuthorizationInfo(securityContext);

        return new SongAggregate(
                song.getSongId(),
                song.getName(),
                song.getAuthor(),
                song.getStreamingUrl(),
                toCommentSummary(comments),
                toRecommendationSummary(recommendations)
        );
    }

    private List<CommentSummary> toCommentSummary(List<Comment> comments) {
        return comments.stream()
                .map(c -> new CommentSummary(c.getCommentId(), c.getAuthor(), c.getContent()))
                .toList();
    }

    private List<RecommendationSummary> toRecommendationSummary(List<Recommendation> recommendations) {
        return recommendations.stream()
                .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                .toList();
    }

    @NotNull
    @Override
    public Mono<Void> deleteSong(int songId) {
        try {
            LOG.debug("delete composite song with songId: {}", songId);

            return Mono.zip(r -> "",
                            getLogAuthorizationInfoMono(),
                            integration.deleteSong(songId),
                            integration.deleteComments(songId),
                            integration.deleteRecommendations(songId)
                    )
                    .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
                    .log(LOG.getName(), Level.FINE)
                    .then();
        }
        catch (RuntimeException re) {
            LOG.debug("delete composite song failed: {}", re.toString());
            throw re;
        }
    }

}
