package backend.fullstack.user.dto;

import backend.fullstack.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between User entities and DTOs.
 *
 * @version 1.0
 * @since 30.03.26
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)   // set in service, not here
    @Mapping(target = "homeLocation", ignore = true)   // set in service, not here
    @Mapping(target = "passwordHash", ignore = true)   // BCrypt in service, never here
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "organizationId",   source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "homeLocationId",   source = "homeLocation.id")
    @Mapping(target = "homeLocationName", source = "homeLocation.name")
    UserResponse toResponse(User user);
}