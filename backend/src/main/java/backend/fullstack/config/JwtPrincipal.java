package backend.fullstack.config;

import java.util.List;

import backend.fullstack.user.role.Role;

/**
 * Authentication principal populated from JWT claims.
 */
public record JwtPrincipal(
        Long userId,
        String email,
        Role role,
        Long organizationId,
        List<Long> locationIds
) {

    public JwtPrincipal {
        locationIds = locationIds == null ? List.of() : List.copyOf(locationIds);
    }
}