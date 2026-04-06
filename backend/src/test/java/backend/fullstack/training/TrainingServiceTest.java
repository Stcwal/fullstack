package backend.fullstack.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.exceptions.AccessDeniedException;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.organization.Organization;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.training.dto.TrainingRecordRequest;
import backend.fullstack.training.dto.TrainingRecordResponse;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRecordRepository trainingRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessContextService accessContext;
    @Mock
    private AuthorizationService authorizationService;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService(
                trainingRecordRepository,
                userRepository,
                accessContext,
                authorizationService
        );
    }

    @Test
    void createCompletedTrainingAutoSetsCompletedAtWhenMissing() {
        User targetUser = user(42L, 100L, Role.STAFF, "staff@everest.no");
        TrainingRecordRequest request = request(42L, TrainingStatus.COMPLETED, null, null);

        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(targetUser));
        when(trainingRecordRepository.save(any(TrainingRecord.class))).thenAnswer(invocation -> {
            TrainingRecord record = invocation.getArgument(0);
            record.setId(9L);
            return record;
        });

        TrainingRecordResponse response = trainingService.create(request);

        ArgumentCaptor<TrainingRecord> captor = ArgumentCaptor.forClass(TrainingRecord.class);
        verify(trainingRecordRepository).save(captor.capture());
        assertNotNull(captor.getValue().getCompletedAt());
        assertEquals(9L, response.getId());
        verify(authorizationService).assertCanManageUser(targetUser);
    }

    @Test
    void getVisibleRecordsFiltersOutUnauthorizedUsers() {
        User actor = user(10L, 100L, Role.MANAGER, "manager@everest.no");
        User self = user(10L, 100L, Role.MANAGER, "manager@everest.no");
        User teammate = user(42L, 100L, Role.STAFF, "staff@everest.no");

        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(accessContext.getCurrentUser()).thenReturn(actor);
        when(trainingRecordRepository.findVisibleRecords(100L, null, null, null))
                .thenReturn(List.of(
                        record(1L, self, TrainingType.GENERAL),
                        record(2L, teammate, TrainingType.CHECKLIST_APPROVAL)
                ));
        when(authorizationService.canViewUser(teammate)).thenReturn(false);

        List<TrainingRecordResponse> visible = trainingService.getVisibleRecords(null, null, null);

        assertEquals(1, visible.size());
        assertEquals(self.getId(), visible.get(0).getUserId());
    }

    @Test
    void getByIdRejectsRecordWhenActorCannotViewTargetUser() {
        User actor = user(10L, 100L, Role.MANAGER, "manager@everest.no");
        User teammate = user(42L, 100L, Role.STAFF, "staff@everest.no");

        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(accessContext.getCurrentUser()).thenReturn(actor);
        when(trainingRecordRepository.findByIdAndUser_Organization_Id(9L, 100L))
                .thenReturn(Optional.of(record(9L, teammate, TrainingType.GENERAL)));
        when(authorizationService.canViewUser(teammate)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> trainingService.getById(9L));
    }

    @Test
    void updateRejectsInvalidDateWindow() {
        User targetUser = user(42L, 100L, Role.STAFF, "staff@everest.no");
        TrainingRecord existing = record(9L, targetUser, TrainingType.GENERAL);
        LocalDateTime completedAt = LocalDateTime.of(2026, 4, 4, 12, 0);
        LocalDateTime expiresAt = completedAt.minusDays(1);

        when(accessContext.getCurrentOrganizationId()).thenReturn(100L);
        when(trainingRecordRepository.findByIdAndUser_Organization_Id(9L, 100L))
                .thenReturn(Optional.of(existing));
        when(userRepository.findById(42L)).thenReturn(Optional.of(targetUser));

        assertThrows(RoleException.class, () -> trainingService.update(
                9L,
                request(42L, TrainingStatus.COMPLETED, completedAt, expiresAt)
        ));

        verify(trainingRecordRepository, never()).save(any());
    }

    private static TrainingRecordRequest request(
            Long userId,
            TrainingStatus status,
            LocalDateTime completedAt,
            LocalDateTime expiresAt
    ) {
        TrainingRecordRequest request = new TrainingRecordRequest();
        request.setUserId(userId);
        request.setTrainingType(TrainingType.GENERAL);
        request.setStatus(status);
        request.setCompletedAt(completedAt);
        request.setExpiresAt(expiresAt);
        return request;
    }

    private static TrainingRecord record(Long id, User user, TrainingType type) {
        return TrainingRecord.builder()
                .id(id)
                .user(user)
                .trainingType(type)
                .status(TrainingStatus.COMPLETED)
                .completedAt(LocalDateTime.of(2026, 4, 4, 10, 0))
                .build();
    }

    private static User user(Long id, Long orgId, Role role, String email) {
        Organization organization = Organization.builder()
                .id(orgId)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return User.builder()
                .id(id)
                .organization(organization)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .passwordHash("hash")
                .role(role)
                .build();
    }
}
