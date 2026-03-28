package backend.fullstack.organization;

import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting between Organization entities and DTOs.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    Organization toEntity(OrganizationRequest request);
    OrganizationResponse toResponse(Organization organization);
}
