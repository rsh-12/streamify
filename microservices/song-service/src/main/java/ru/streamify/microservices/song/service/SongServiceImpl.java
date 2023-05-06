package ru.streamify.microservices.song.service;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.streamify.api.core.song.Song;
import ru.streamify.api.core.song.SongService;
import ru.streamify.api.exception.InvalidInputException;
import ru.streamify.api.exception.NotFoundException;
import ru.streamify.microservices.song.entity.SongEntity;
import ru.streamify.microservices.song.repository.SongRepository;

import static java.util.logging.Level.FINE;

@RestController
public class SongServiceImpl implements SongService {
    private static final Logger LOG = LoggerFactory.getLogger(SongServiceImpl.class);

    private final SongRepository repository;
    private final SongMapper mapper;

    @Autowired
    public SongServiceImpl(SongRepository repository, SongMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    public Mono<Song> getSong(int songId) {
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        LOG.info("Will get song info for id={}", songId);

        return repository.findBySongId(songId)
                .switchIfEmpty(Mono.error(new NotFoundException("No song found for songId: " + songId)))
                .log(LOG.getName(), FINE)
                .map(mapper::entityToApi);
    }

    @NotNull
    @Override
    public Mono<Song> createSong(@NotNull Song body) {
        int songId = body.getSongId();
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        SongEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Song Id: " + songId)
                ).map(mapper::entityToApi);
    }

    @NotNull
    @Override
    public Mono<Void> deleteSong(int songId) {
        if (songId < 1) {
            throw new InvalidInputException("Invalid songId: " + songId);
        }

        LOG.debug("deleteSong: tries to delete an entity with songId: {}", songId);

        return repository.findBySongId(songId)
                .log(LOG.getName(), FINE)
                .map(repository::delete)
                .flatMap(e -> e);
    }
}
