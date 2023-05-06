package ru.streamify.microservices.song;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.streamify.api.core.song.Song;
import ru.streamify.microservices.song.entity.SongEntity;
import ru.streamify.microservices.song.service.SongMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperTests {
    private final SongMapper mapper = Mappers.getMapper(SongMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Song api = new Song(1, "n", "a", "s");
        SongEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getSongId(), entity.getSongId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getStreamingUrl(), entity.getStreamingUrl());

        Song api2 = mapper.entityToApi(entity);

        assertEquals(api.getSongId(), api2.getSongId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getStreamingUrl(), api2.getStreamingUrl());
    }
}
