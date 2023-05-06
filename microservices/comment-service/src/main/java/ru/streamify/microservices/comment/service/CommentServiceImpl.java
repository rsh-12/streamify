package ru.streamify.microservices.comment.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.api.core.comment.CommentService;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.microservices.comment.entity.CommentEntity;
import ru.streamify.microservices.comment.repository.CommentRepository;

import static java.util.logging.Level.FINE;

@RestController
public class CommentServiceImpl implements CommentService {
    private static final Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);
    private final CommentRepository repository;
    private final CommentMapper mapper;

    @Autowired
    public CommentServiceImpl(CommentRepository repository, CommentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    public Flux<Comment> getComments(int songId) {
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        LOG.info("Will get comments for song with id={}", songId);

        return repository.findBySongId(songId)
                .log(LOG.getName(), FINE)
                .map(mapper::entityToApi);
    }

    @NotNull
    @Override
    public Mono<Comment> createComment(@NotNull Comment body) {
        if (body.getSongId() < 1) {
            throw new InvalidInputException("Invalid songId: " + body.getSongId());
        }

        CommentEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException(
                                "Duplicate key, Song Id: " + body.getSongId()
                                        + ", Comment Id: " + body.getCommentId()))
                .map(mapper::entityToApi);
    }

    @NotNull
    @Override
    public Mono<Void> deleteComments(int songId) {
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        LOG.debug("deleteComments: tries to delete comments for the song with songId: {}", songId);
        return repository.deleteAll(repository.findBySongId(songId));
    }
}
