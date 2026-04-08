package backend.fullstack.deviations.api.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import backend.fullstack.deviations.domain.Deviation;

@Mapper(componentModel = "spring")
public interface DeviationMapper {

    @Mapping(target = "reportedBy", source = "reportedByName")
    @Mapping(target = "resolvedBy", source = "resolvedByName")
    @Mapping(target = "reportedAt", source = "createdAt")
    DeviationResponse toResponse(Deviation deviation);
}
