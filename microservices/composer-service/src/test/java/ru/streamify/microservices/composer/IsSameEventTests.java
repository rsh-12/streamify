package ru.streamify.microservices.composer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.streamify.api.core.song.Song;
import ru.streamify.api.event.Event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static ru.streamify.api.event.Event.Type.CREATE;
import static ru.streamify.api.event.Event.Type.DELETE;
import static ru.streamify.microservices.composer.IsSameEvent.sameEventExceptCreatedAt;

class IsSameEventTests {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {
        Event<Integer, Song> event1 = new Event<>(CREATE, 1, new Song(1, "name", "a", ""));
        Event<Integer, Song> event2 = new Event<>(CREATE, 1, new Song(1, "name", "a", ""));
        Event<Integer, Song> event3 = new Event<>(DELETE, 1, null);
        Event<Integer, Song> event4 = new Event<>(CREATE, 1, new Song(2, "name", "a", ""));

        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
