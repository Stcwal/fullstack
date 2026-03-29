package backend.fullstack.organization;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;

/**
 * Mapper interface for converting between Organization entities and DTOs.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Organization toEntity(OrganizationRequest request);

    @Mapping(target = "locationsList", source = "locations")
    @Mapping(target = "locationCount", expression = "java(organization.getLocations() != null ? organization.getLocations().size() : 0)")
    OrganizationResponse toResponse(Organization organization);
}
