package backend.fullstack.user;

import backend.fullstack.exceptions.*;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.location.dto.AssignLocationsRequest;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.permission.model.PermissionScope;
import backend.fullstack.permission.override.UserPermissionOverride;
import backend.fullstack.permission.override.UserPermissionOverrideRepository;
import backend.fullstack.permission.profile.PermissionProfile;
import backend.fullstack.permission.profile.PermissionProfileRepository;
import backend.fullstack.permission.profile.UserProfileAssignment;
import backend.fullstack.permission.profile.UserProfileAssignmentRepository;
import backend.fullstack.user.dto.ChangePasswordRequest;
import backend.fullstack.user.dto.CreateUserRequest;
import backend.fullstack.user.dto.UpdateUserProfileRequest;
import backend.fullstack.user.dto.UserMapper;
import backend.fullstack.user.dto.UserResponse;
import backend.fullstack.user.role.Role;
import backend.fullstack.user.role.UpdateRoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing users within an organization.
 * All operations are scoped to the caller's organization — cross-tenant
 * access is structurally prevented by verifying organization ownership
 * before every operation.
 *
 * @version 1.0
 * @since 30.03.26
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final UserMapper userMapper;
    private final AccessContextService accessContext;
    private final AuthorizationService authorizationService;
    private final PermissionProfileRepository permissionProfileRepository;
    private final UserProfileAssignmentRepository userProfileAssignmentRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns all users belonging to the caller's organization.
     * Only ADMIN users may call this.
     *
     * @return list of all users in the organization
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllInOrganization() {
        if (!authorizationService.hasPermission(Permission.USERS_READ_ORGANIZATION)
                && !authorizationService.hasPermission(Permission.USERS_READ_LOCATION)) {
            throw new AccessDeniedException("Missing permission: users.read.location or users.read.organization");
        }

        Long orgId = accessContext.getCurrentOrganizationId();
        return userRepository.findByOrganization_Id(orgId)
                .stream()
            .filter(authorizationService::canViewUser)
                .map(this::toResponseWithLocations)
                .toList();
    }

    /**
     * Returns a single user by ID.
     * Verifies the user belongs to the caller's organization before returning.
     *
     * @param id the user ID to look up
     * @return the user response DTO
     * @throws ResourceNotFoundException if the user does not exist in this organization
     */
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanViewUser(user);
        return toResponseWithLocations(user);
    }

    /**
     * Returns the currently authenticated user's own profile.
     *
     * @return the current user's response DTO
     */
    @Transactional(readOnly = true)
    public UserResponse getMyProfile() {
        return toResponseWithLocations(accessContext.getCurrentUser());
    }

    /**
     * Creates a new user within the caller's organization.
     * The password is hashed with BCrypt before persisting.
     * MANAGER and STAFF roles require a locationId in the request.
     *
     * @param request the user creation request
     * @return the created user as a response DTO
     * @throws UserConflictException   if the email is already registered
     */
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        authorizationService.assertCanCreateUser(request.getRole(), request.getLocationId());

        // 1. Email must be globally unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserConflictException("A user with this email already exists");
        }

        Long orgId = accessContext.getCurrentOrganizationId();

        // 2. Build the entity from the DTO
        User user = userMapper.toEntity(request);
        user.setOrganization(accessContext.getCurrentUser().getOrganization());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        // 3. Resolve location based on role
        assignLocationForRole(user, request.getRole(), request.getLocationId(), orgId);

        return toResponseWithLocations(userRepository.save(user));
    }


    /**
     * Updates a user's first name and/or last name.
     * Email, password, and role cannot be changed through this method.
     *
     * @param id      the ID of the user to update
     * @param request the update request containing new name fields
     * @return the updated user response DTO
     */
    @Transactional
    public UserResponse updateProfile(Long id, UpdateUserProfileRequest request) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanManageUser(user);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)  user.setLastName(request.getLastName());

        return toResponseWithLocations(userRepository.save(user));
    }

    /**
     * Changes a user's role and updates their location assignment accordingly.
     *
     * <p>Rules:
     * <ul>
     *   <li>MANAGER and STAFF must have a locationId provided</li>
     *   <li>ADMIN and SUPERVISOR are org-level — their homeLocation is cleared</li>
     *   <li>The last ADMIN in the organization cannot be demoted</li>
     * </ul>
     *
     * @param id      the ID of the user whose role is being changed
     * @param request contains the new role and optionally a locationId
     * @return the updated user response DTO
     * @throws backend.fullstack.exceptions.RoleException if trying to demote the last admin, or location is missing
     *                           for location-scoped roles
     */
    @Transactional
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanChangeRole(user, request.getRole(), request.getLocationId());

        Long orgId = accessContext.getCurrentOrganizationId();

        // Guard: cannot demote the last admin
        if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            long adminCount = userRepository
                    .findByOrganization_IdAndRole(orgId, Role.ADMIN)
                    .size();
            if (adminCount <= 1) {
                throw new RoleException(
                        "Cannot change role of the last ADMIN in the organization"
                );
            }
        }

        // Clear existing location state before reassigning
        user.setHomeLocation(null);
        user.getLocations().clear();
        user.setRole(request.getRole());

        assignLocationForRole(user, request.getRole(), request.getLocationId(), orgId);

        return toResponseWithLocations(userRepository.save(user));
    }

    /**
     * Assigns or replaces the set of additional locations for a SUPERVISOR or STAFF/MANAGER user.
     * All provided location IDs must belong to the caller's organization.
     *
     * @param id      the ID of the user to update
     * @param request contains the list of location IDs to assign
     * @return the updated user response DTO
     * @throws LocationException if any location does not belong to this organization
     */
    @Transactional
    public UserResponse assignAdditionalLocations(Long id, AssignLocationsRequest request) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanAssignLocations(user, request.getLocationIds());

        Long orgId = accessContext.getCurrentOrganizationId();

        List<Location> locations = request.getLocationIds().stream()
                .map(locId -> locationRepository
                        .findByIdAndOrganizationId(locId, orgId)
                        .orElseThrow(() -> new LocationException(
                                "Location " + locId + " does not exist in your organization"
                        )))
                .toList();

        user.getLocations().clear();
        user.getLocations().addAll(locations);

        return toResponseWithLocations(userRepository.save(user));
    }

    /**
     * Changes a user's password after verifying their current password.
     * Users can only change their own password through this method.
     *
     * @param request request contains the current and new password
     * @throws PasswordException if the current password is incorrect
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = accessContext.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new PasswordException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Soft-deletes a user by setting their account to inactive.
     * The user record is preserved for audit trail and data integrity.
     * An ADMIN cannot deactivate themselves.
     *
     * @param id the ID of the user to deactivate
     * @throws OrganizationConflictException if the user tries to deactivate their own account
     */
    @Transactional
    public void deactivate(Long id) {
        User user     = findInCurrentOrg(id);
        authorizationService.assertCanDeactivateUser(user);

        User caller   = accessContext.getCurrentUser();
        Long orgId = accessContext.getCurrentOrganizationId();

        if (user.getId().equals(caller.getId())) {
            throw new OrganizationConflictException("You cannot deactivate your own account");
        }

        if (user.getRole() == Role.ADMIN && user.isActive()) {
            long activeAdminCount = userRepository.countByOrganization_IdAndRoleAndIsActiveTrue(orgId, Role.ADMIN);
            if (activeAdminCount <= 1) {
                throw new RoleException("Cannot deactivate the last active ADMIN in the organization");
            }
        }

        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Reactivates a previously deactivated user account.
     *
     * @param id the ID of the user to reactivate
     */
    @Transactional
    public void reactivate(Long id) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanManageUser(user);

        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void assignProfiles(
            Long id,
            List<Long> profileIds,
            Long locationId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            boolean replaceScopeAssignments
    ) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanManageUser(user);
        validateWindow(startsAt, endsAt);

        if (profileIds == null || profileIds.isEmpty()) {
            throw new RoleException("At least one profile id must be provided");
        }

        Long orgId = accessContext.getCurrentOrganizationId();
        Location scopedLocation = null;
        if (locationId != null) {
            scopedLocation = locationRepository.findByIdAndOrganizationId(locationId, orgId)
                    .orElseThrow(() -> new LocationException("Location does not exist in your organization"));

            if (accessContext.getCurrentRole() != Role.ADMIN) {
                accessContext.assertCanAccess(locationId);
            }
        }

        List<PermissionProfile> profiles = permissionProfileRepository
                .findByIdInAndOrganization_IdAndIsActiveTrue(profileIds, orgId);

        if (profiles.size() != profileIds.size()) {
            throw new RoleException("One or more profiles are invalid for this organization");
        }

        if (replaceScopeAssignments) {
            if (locationId == null) {
                userProfileAssignmentRepository.deleteAllGlobalByUserId(user.getId());
            } else {
                userProfileAssignmentRepository.deleteAllByUserIdAndLocationId(user.getId(), locationId);
            }
        }

        for (PermissionProfile profile : profiles) {
            userProfileAssignmentRepository.save(
                    UserProfileAssignment.builder()
                            .user(user)
                            .profile(profile)
                            .location(scopedLocation)
                            .startsAt(startsAt)
                            .endsAt(endsAt)
                            .build()
            );
        }
    }

    @Transactional
    public void addUserPermissionGrant(Long id, Permission permission, String reason) {
        addUserPermissionOverride(id, permission, PermissionEffect.ALLOW, PermissionScope.ORGANIZATION, null, null, null, reason);
    }

    @Transactional
    public void addUserPermissionDeny(Long id, Permission permission, String reason) {
        addUserPermissionOverride(id, permission, PermissionEffect.DENY, PermissionScope.ORGANIZATION, null, null, null, reason);
    }

    @Transactional
    public void assignTemporaryLocationScope(
            Long id,
            Long locationId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            TemporaryAssignmentMode mode,
            String reason
    ) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanManageUser(user);
        validateWindow(startsAt, endsAt);

        Long orgId = accessContext.getCurrentOrganizationId();
        Location location = locationRepository.findByIdAndOrganizationId(locationId, orgId)
                .orElseThrow(() -> new LocationException("Location does not exist in your organization"));

        if (accessContext.getCurrentRole() != Role.ADMIN) {
            accessContext.assertCanAccess(locationId);
        }

        LocalDateTime now = LocalDateTime.now();
        TemporaryAssignmentStatus initialStatus = startsAt != null && startsAt.isAfter(now)
                ? TemporaryAssignmentStatus.SCHEDULED
                : TemporaryAssignmentStatus.ACTIVE;

        userLocationScopeAssignmentRepository.save(
                UserLocationScopeAssignment.builder()
                        .user(user)
                        .location(location)
                        .startsAt(startsAt)
                        .endsAt(endsAt)
                        .mode(mode == null ? TemporaryAssignmentMode.INHERIT : mode)
                        .status(initialStatus)
                        .reason(reason)
                        .build()
        );
    }

    @Transactional
    public void completeTemporaryLocationScope(Long userId, Long assignmentId) {
        User user = findInCurrentOrg(userId);
        authorizationService.assertCanManageUser(user);

        UserLocationScopeAssignment assignment = userLocationScopeAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Temporary location assignment not found"));

        if (assignment.getStatus() == TemporaryAssignmentStatus.ARCHIVED
                || assignment.getStatus() == TemporaryAssignmentStatus.CONFIRMED) {
            throw new RoleException("Assignment is already closed");
        }

        assignment.setStatus(TemporaryAssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());
        userLocationScopeAssignmentRepository.save(assignment);
    }

    @Transactional
    public void confirmTemporaryLocationScope(Long userId, Long assignmentId) {
        User user = findInCurrentOrg(userId);
        authorizationService.assertCanManageUser(user);

        UserLocationScopeAssignment assignment = userLocationScopeAssignmentRepository.findById(assignmentId)
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Temporary location assignment not found"));

        if (assignment.getStatus() != TemporaryAssignmentStatus.COMPLETED) {
            throw new RoleException("Assignment must be completed before confirmation");
        }

        assignment.setStatus(TemporaryAssignmentStatus.CONFIRMED);
        assignment.setConfirmedAt(LocalDateTime.now());
        userLocationScopeAssignmentRepository.save(assignment);
    }

    @Transactional
    public void assignTemporaryPermission(
            Long id,
            Permission permission,
            PermissionEffect effect,
            PermissionScope scope,
            Long locationId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            String reason
    ) {
        addUserPermissionOverride(id, permission, effect, scope, locationId, startsAt, endsAt, reason);
    }

    /**
     * Resolves and assigns location(s) to a user based on their role.
     * MANAGER and STAFF require a home location. ADMIN and SUPERVISOR do not.
     *
     * @param user       the user entity to modify
     * @param role       the role being assigned
     * @param locationId the requested location ID (may be null for org-level roles)
     * @param orgId      the organization ID used to verify location ownership
     */
    private void assignLocationForRole(User user, Role role, Long locationId, Long orgId) {
        if (role == Role.MANAGER || role == Role.STAFF) {
            if (locationId == null) {
                throw new RoleException(
                        "A location must be specified for " + role + " role"
                );
            }
            Location location = locationRepository
                    .findByIdAndOrganizationId(locationId, orgId)
                    .orElseThrow(() -> new LocationException(
                            "Location does not exist in your organization"
                    ));
            user.setHomeLocation(location);
        }
        // ADMIN and SUPERVISOR are org-wide — no home location needed
    }

    /**
     * Loads a user by ID and verifies they belong to the current user's organization.
     * This is the primary cross-tenant guard for all user operations.
     *
     * @param userId the ID of the user to find
     * @return the User entity
     * @throws ResourceNotFoundException if the user does not exist or belongs to a different organization
     */
    private User findInCurrentOrg(Long userId) {
        Long orgId = accessContext.getCurrentOrganizationId();
        return userRepository.findById(userId)
                .filter(u -> u.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Converts a User entity to a UserResponse and manually populates
     * the additionalLocationIds field, which MapStruct cannot resolve automatically.
     *
     * @param user the User entity to convert
     * @return the populated UserResponse DTO
     */
    private UserResponse toResponseWithLocations(User user) {
        UserResponse response = userMapper.toResponse(user);
        response.setAdditionalLocationIds(
                user.getLocations().stream()
                        .map(Location::getId)
                        .toList()
        );
        return response;
    }

    private void addUserPermissionOverride(
            Long id,
            Permission permission,
            PermissionEffect effect,
            PermissionScope scope,
            Long locationId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            String reason
    ) {
        User user = findInCurrentOrg(id);
        authorizationService.assertCanManageUser(user);
        validateWindow(startsAt, endsAt);

        if (scope == PermissionScope.LOCATION) {
            if (locationId == null) {
                throw new RoleException("locationId is required when scope is LOCATION");
            }

            Long orgId = accessContext.getCurrentOrganizationId();
            locationRepository.findByIdAndOrganizationId(locationId, orgId)
                    .orElseThrow(() -> new LocationException("Location does not exist in your organization"));

            if (accessContext.getCurrentRole() != Role.ADMIN) {
                accessContext.assertCanAccess(locationId);
            }
        }

        userPermissionOverrideRepository.save(
                UserPermissionOverride.builder()
                        .user(user)
                        .permission(permission)
                        .effect(effect)
                        .scope(scope)
                        .locationId(locationId)
                        .startsAt(startsAt)
                        .endsAt(endsAt)
                        .reason(reason)
                        .build()
        );
    }

    private void validateWindow(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw new RoleException("endsAt must be after startsAt");
        }
    }
}
