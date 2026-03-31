package backend.fullstack.permission;

/**
 * Context for condition-based permission checks.
 *
 * The context can be extended as profile- and training-based rules are added.
 * 
 * @version 1.0
 * @since 31.03.26
 */
public record PermissionConditionContext(
        boolean trainingCompleted,
        boolean approvalRequired
) {

    public static PermissionConditionContext empty() {
        return new PermissionConditionContext(true, false);
    }
}
