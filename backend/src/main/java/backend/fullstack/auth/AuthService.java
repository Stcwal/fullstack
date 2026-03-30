package backend.fullstack.auth;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

<<<<<<< Updated upstream
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
=======
import backend.fullstack.exceptions.LocationException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.exceptions.UserConflictException;
import backend.fullstack.location.Location;
>>>>>>> Stashed changes
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

/**
 * Service class handling authentication-related operations such as user registration and login response construction.
 *
 * @version 1.0
 * @since 29.03.26
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            LocationRepository locationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.locationRepository = locationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers the first ADMIN user during application bootstrap. This method can only be called if there are no existing users in the system.
     *
     * @param request The registration request containing user details and organization/location associations.
     * @return The created User entity representing the bootstrap admin.
     */
    public User registerBootstrapAdmin(RegisterRequest request) {
        if (userRepository.count() > 0) {
            throw new AccessDeniedException("Public registration is disabled after bootstrap");
        }

        if (request.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Bootstrap registration can only create an ADMIN user");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        User user = User.builder()
                .organization(organization)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        if (request.getPrimaryLocationId() != null) {
            Location location = locationRepository.findById(request.getPrimaryLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Primary location not found"));

            if (!location.getOrganizationId().equals(organization.getId())) {
                throw new IllegalArgumentException("Primary location does not belong to the organization");
            }

            user.setHomeLocation(location);
        }

        return userRepository.save(user);
    }

    /**
     * Builds a LoginResponse object containing user details and allowed location IDs based on the user's role and associations. For ADMIN users, all location IDs within the organization are included. For non-ADMIN users, the home location ID and any additional location IDs associated with the user are included, ensuring no duplicates.
     *
     * @param user The authenticated User entity for which the login response is being built.
     * @return A LoginResponse object containing the user's ID, email, role, organization ID, primary location ID, and a list of allowed location IDs.
     */
    public LoginResponse buildLoginResponse(User user) {
        List<Long> allowedLocationIds = resolveAllowedLocationIds(user);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setOrganizationId(user.getOrganizationId());
        response.setPrimaryLocationId(user.getHomeLocationId());
        response.setAllowedLocationIds(allowedLocationIds);

        return response;
    }

    /**
     * Resolves the list of allowed location IDs for a given user. For ADMIN users, this includes all location IDs associated with the user's organization. For non-ADMIN users, this includes the home location ID (if set) and any additional location IDs associated with the user, ensuring that there are no duplicates in the final list.
     *
     * @param user The User entity for which to resolve allowed location IDs.
     * @return A list of allowed location IDs that the user has access to.
     */
    private List<Long> resolveAllowedLocationIds(User user) {
        if (user.getRole() == Role.ADMIN) {
            return locationRepository.findIdsByOrganizationId(user.getOrganizationId());
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();
        if (user.getHomeLocationId() != null) {
            uniqueIds.add(user.getHomeLocationId());
        }

        uniqueIds.addAll(userRepository.findAdditionalLocationIdsByUserId(user.getId()));
        return new ArrayList<>(uniqueIds);
    }
}
