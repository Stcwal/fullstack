package backend.fullstack.permission.definition;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import backend.fullstack.permission.catalog.DefaultRolePermissionMatrix;
import backend.fullstack.permission.catalog.RolePermissionBinding;
import backend.fullstack.permission.catalog.RolePermissionBindingRepository;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.user.role.Role;

@Component
public class PermissionBootstrapSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PermissionBootstrapSeeder.class);

    private final PermissionDefinitionRepository permissionDefinitionRepository;
    private final RolePermissionBindingRepository rolePermissionBindingRepository;

    public PermissionBootstrapSeeder(
            PermissionDefinitionRepository permissionDefinitionRepository,
            RolePermissionBindingRepository rolePermissionBindingRepository
    ) {
        this.permissionDefinitionRepository = permissionDefinitionRepository;
        this.rolePermissionBindingRepository = rolePermissionBindingRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedPermissionDefinitions();
            seedRolePermissions();
        } catch (RuntimeException ex) {
            // Startup should not fail solely because permission seed cannot run.
            logger.warn("Permission bootstrap seed skipped due to repository/database issue", ex);
        }
    }

    private void seedPermissionDefinitions() {
        for (Permission permission : Permission.values()) {
            permissionDefinitionRepository.findByPermissionKey(permission.key())
                    .orElseGet(() -> permissionDefinitionRepository.save(
                            PermissionDefinition.builder()
                                    .permissionKey(permission.key())
                                    .description(permission.key())
                                    .build()
                    ));
        }
    }

    private void seedRolePermissions() {
        Map<Role, Set<Permission>> defaults = DefaultRolePermissionMatrix.create();

        for (Map.Entry<Role, Set<Permission>> entry : defaults.entrySet()) {
            Role role = entry.getKey();
            if (rolePermissionBindingRepository.existsByRole(role)) {
                continue;
            }

            for (Permission permission : entry.getValue()) {
                PermissionDefinition definition = permissionDefinitionRepository
                        .findByPermissionKey(permission.key())
                        .orElseThrow(() -> new IllegalStateException("Permission definition missing: " + permission.key()));

                rolePermissionBindingRepository.save(
                        RolePermissionBinding.builder()
                                .role(role)
                                .permission(definition)
                                .build()
                );
            }
        }
    }
}
