package backend.fullstack.organization.dto;

import backend.fullstack.organization.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between Organization entities and DTOs.
 *
 * @version 1.0
 * @since 30.03.26
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Organization toEntity(OrganizationRequest request);

    OrganizationResponse toResponse(Organization organization);
}
