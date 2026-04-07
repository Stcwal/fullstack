package backend.fullstack.permission.model;

/**
 * Enum representing the effect of a permission override. This indicates whether a specific permission is allowed or denied for a user, role, or profile.
 * 
 * The PermissionEffect enum is used in various contexts within the permission management system to determine how permissions are applied and evaluated. For example, when assigning permissions to a user or role, the effect can be set to ALLOW to grant the permission or DENY to explicitly deny it, even if it would otherwise be granted through other means (such as role membership).
 *
 * @version 1.0
 * @since 31.03.26
 */
public enum PermissionEffect {
    ALLOW,
    DENY
}
