package backend.fullstack.permission;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import backend.fullstack.permission.catalog.RolePermissionBinding;
import backend.fullstack.permission.catalog.RolePermissionBindingRepository;
import backend.fullstack.permission.definition.PermissionDefinition;
import backend.fullstack.permission.definition.PermissionDefinitionRepository;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class PermissionBootstrapSeederTest {

    @Mock
    private PermissionDefinitionRepository permissionDefinitionRepository;

    @Mock
    private RolePermissionBindingRepository rolePermissionBindingRepository;

    private PermissionBootstrapSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new PermissionBootstrapSeeder(permissionDefinitionRepository, rolePermissionBindingRepository);
    }

    @Test
    void runCreatesMissingPermissionDefinitions() {
        when(permissionDefinitionRepository.findByPermissionKey(anyString())).thenReturn(Optional.empty());
        when(permissionDefinitionRepository.save(any(PermissionDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(rolePermissionBindingRepository.existsByRole(any(Role.class))).thenReturn(true);

        seeder.run(mock(ApplicationArguments.class));

        ArgumentCaptor<PermissionDefinition> definitionCaptor = ArgumentCaptor.forClass(PermissionDefinition.class);
        verify(permissionDefinitionRepository, times(Permission.values().length)).save(definitionCaptor.capture());
        verify(rolePermissionBindingRepository, never()).save(any(RolePermissionBinding.class));

        Set<String> savedKeys = definitionCaptor.getAllValues().stream()
                .map(PermissionDefinition::getPermissionKey)
                .collect(Collectors.toSet());

        assertEquals(Permission.values().length, savedKeys.size());
    }

    @Test
    void runSeedsBindingsOnlyForMissingRoleBaselines() {
        when(permissionDefinitionRepository.findByPermissionKey(anyString()))
                .thenAnswer(invocation -> Optional.of(
                        PermissionDefinition.builder().permissionKey(invocation.getArgument(0)).build()
                ));
        when(rolePermissionBindingRepository.existsByRole(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0) != Role.MANAGER);

        seeder.run(mock(ApplicationArguments.class));

        verify(permissionDefinitionRepository, never()).save(any(PermissionDefinition.class));
        verify(rolePermissionBindingRepository, atLeastOnce())
                .save(any(RolePermissionBinding.class));
        verify(rolePermissionBindingRepository, never())
                .save(org.mockito.ArgumentMatchers.argThat(binding -> binding.getRole() == Role.ADMIN));
        verify(rolePermissionBindingRepository, atLeastOnce())
                .save(org.mockito.ArgumentMatchers.argThat(binding -> binding.getRole() == Role.MANAGER));
    }

    @Test
    void runSwallowsRuntimeExceptionsFromRepositories() {
        when(permissionDefinitionRepository.findByPermissionKey(anyString()))
                .thenThrow(new RuntimeException("db not available"));

        assertDoesNotThrow(() -> seeder.run(mock(ApplicationArguments.class)));
    }
}
