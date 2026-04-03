package backend.fullstack.permission.model;

/**
 * Enum representing the types of conditions that can be associated with a permission. These conditions define additional requirements that must be met for a permission to be considered effective.
 * 
 * The PermissionConditionType enum is used in the context of permission profile bindings to specify any special conditions that apply to a permission assignment. For example, a permission may require that the user has completed specific training or has received approval before it can be granted.
 *
 * @version 1.0
 * @since 31.03.26
 */
public enum PermissionConditionType {
    NONE,
    TRAINING_REQUIRED,
    APPROVAL_REQUIRED
}
