package ru.streamify.microservices.song.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.streamify.api.core.song.Song;
import ru.streamify.microservices.song.entity.SongEntity;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface SongMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    SongEntity apiToEntity(Song api);

    Song entityToApi(SongEntity entity);

}
