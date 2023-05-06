package ru.streamify.microservices.recommendation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.api.core.recommendation.RecommendationService;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.EventProcessingException;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            Event.Type eventType = event.getEventType();
            assert eventType != null;

            switch (eventType) {
                case CREATE -> {
                    Recommendation recommendation = event.getData();
                    assert recommendation != null;
                    LOG.info("Create recommendation with ID: {}", recommendation.getSongId());
                    recommendationService.createRecommendation(recommendation).block();
                }
                case DELETE -> {
                    Integer songId = event.getKey();
                    assert songId != null;
                    LOG.info("Delete recommendations with songId: {}", songId);
                    recommendationService.deleteRecommendations(songId).block();
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
