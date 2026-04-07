package backend.fullstack.units.api.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import backend.fullstack.units.domain.TemperatureUnit;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TemperatureUnit toEntity(UnitRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    UnitResponse toResponse(TemperatureUnit unit);
}
