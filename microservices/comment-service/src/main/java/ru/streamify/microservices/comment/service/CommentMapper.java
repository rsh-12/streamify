package ru.streamify.microservices.comment.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.streamify.api.core.comment.Comment;
import ru.streamify.microservices.comment.entity.CommentEntity;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CommentMapper {

    Comment entityToApi(CommentEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    CommentEntity apiToEntity(Comment api);

    List<Comment> entityListToApiList(List<CommentEntity> entity);

    List<CommentEntity> apiListToEntityList(List<Comment> api);
}
