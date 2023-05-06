package ru.streamify.microservices.recommendation;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.microservices.recommendation.entity.RecommendationEntity;
import ru.streamify.microservices.recommendation.service.RecommendationMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperTests {
    private final RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Recommendation api = new Recommendation(1, 2, "a", 4, "C");
        RecommendationEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getSongId(), entity.getSongId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRate());
        assertEquals(api.getContent(), entity.getContent());

        Recommendation api2 = mapper.entityToApi(entity);

        assertEquals(api.getSongId(), api2.getSongId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRate(), api2.getRate());
        assertEquals(api.getContent(), api2.getContent());
    }

    @Test
    void mapperListTests() {
        assertNotNull(mapper);

        Recommendation api = new Recommendation(1, 2, "a", 4, "C");
        List<Recommendation> apiList = Collections.singletonList(api);

        List<RecommendationEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        RecommendationEntity entity = entityList.get(0);

        assertEquals(api.getSongId(), entity.getSongId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRate());
        assertEquals(api.getContent(), entity.getContent());

        List<Recommendation> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Recommendation api2 = api2List.get(0);

        assertEquals(api.getSongId(), api2.getSongId());
        assertEquals(api.getRecommendationId(), api2.getRecommendationId());
        assertEquals(api.getAuthor(), api2.getAuthor());
        assertEquals(api.getRate(), api2.getRate());
        assertEquals(api.getContent(), api2.getContent());
    }

}
