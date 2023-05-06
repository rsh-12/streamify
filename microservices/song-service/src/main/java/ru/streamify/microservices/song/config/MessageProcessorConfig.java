package ru.streamify.microservices.song.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.streamify.api.core.song.Song;
import ru.streamify.api.core.song.SongService;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.EventProcessingException;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final SongService songService;

    @Autowired
    public MessageProcessorConfig(SongService songService) {
        this.songService = songService;
    }

    @Bean
    public Consumer<Event<Integer, Song>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            Event.Type eventType = event.getEventType();
            assert eventType != null;

            switch (eventType) {
                case CREATE -> {
                    Song song = event.getData();
                    assert song != null;
                    LOG.info("Create song with ID: {}", song.getSongId());
                    songService.createSong(song).block();
                }
                case DELETE -> {
                    Integer songId = event.getKey();
                    assert songId != null;
                    LOG.info("Delete recommendations with songId: {}", songId);
                    songService.deleteSong(songId).block();
                }
                default -> {
                    String errorMessage = "Incorrect event type: " + eventType + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            LOG.info("Message processing done!");
        };
    }

}
