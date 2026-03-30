package backend.fullstack.location.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import backend.fullstack.location.Location;

/**
 * Mapper interface for converting between Location entities and DTOs.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Location toEntity(LocationRequest request);

    LocationResponse toResponse(Location location);
}