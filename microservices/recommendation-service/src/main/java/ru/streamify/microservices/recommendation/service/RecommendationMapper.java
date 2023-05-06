package ru.streamify.microservices.recommendation.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.streamify.api.core.recommendation.Recommendation;
import ru.streamify.microservices.recommendation.entity.RecommendationEntity;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface RecommendationMapper {

    Recommendation entityToApi(RecommendationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityListToApiList(List<RecommendationEntity> entities);

    List<RecommendationEntity> apiListToEntityList(List<Recommendation> api);

}
