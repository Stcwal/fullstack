package backend.fullstack.location.dto;

import backend.fullstack.location.Location;

import org.mapstruct.Mapper;

/**
 * Mapper interface for converting between Location entities and DTOs.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationRequest request);

    LocationResponse toResponse(Location location);
}