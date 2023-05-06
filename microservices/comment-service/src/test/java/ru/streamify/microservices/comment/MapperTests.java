package ru.streamify.microservices.comment;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.microservices.comment.entity.CommentEntity;
import ru.streamify.microservices.comment.service.CommentMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperTests {
    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Comment api = new Comment(1, 2, "a", "c");
        CommentEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getSongId(), entity.getSongId());
        assertEquals(api.getCommentId(), entity.getCommentId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getContent(), entity.getContent());

        Comment api2 = mapper.entityToApi(entity);

        assertEquals(api.getSongId(), api2.getSongId());
        assertEquals(api.getCommentId(), api2.getCommentId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getContent(), api2.getContent());
    }

    @Test
    void mapperListTests() {
        assertNotNull(mapper);

        Comment api = new Comment(1, 2, "a", "c");
        List<Comment> apiList = Collections.singletonList(api);

        List<CommentEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        CommentEntity entity = entityList.get(0);

        assertEquals(api.getSongId(), entity.getSongId());
        assertEquals(api.getCommentId(), entity.getCommentId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getContent(), entity.getContent());

        List<Comment> apiList2 = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), apiList2.size());

        Comment api2 = apiList2.get(0);

        assertEquals(api.getSongId(), api2.getSongId());
        assertEquals(api.getCommentId(), api2.getCommentId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getContent(), api2.getContent());
    }
}
