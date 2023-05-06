package ru.streamify.microservices.comment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.api.core.comment.CommentService;
import ru.streamify.api.event.Event;
import ru.streamify.api.exception.EventProcessingException;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final CommentService commentService;

    @Autowired
    public MessageProcessorConfig(CommentService commentService) {
        this.commentService = commentService;
    }

    @Bean
    public Consumer<Event<Integer, Comment>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            Event.Type eventType = event.getEventType();
            assert eventType != null;

            switch (eventType) {
                case CREATE -> createComment(event);
                case DELETE -> deleteComment(event);
                default -> throwException(eventType);
            }

            LOG.info("Message processing done!");
        };
    }

    private void createComment(Event<Integer, Comment> event) {
        Comment comment = event.getData();
        assert comment != null;
        LOG.info("Create comment with ID: {}", comment.getSongId());
        commentService.createComment(comment).block();
    }

    private void deleteComment(Event<Integer, Comment> event) {
        Integer songId = event.getKey();
        assert songId != null;
        LOG.info("Delete comments with songId: {}", songId);
        commentService.deleteComments(songId).block();
    }

    private void throwException(Event.Type eventType) {
        String errorMessage = "Incorrect event type: " + eventType + ", expected a CREATE or DELETE event";
        LOG.warn(errorMessage);
        throw new EventProcessingException(errorMessage);
    }
}
