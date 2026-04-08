package backend.fullstack.deviations.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.deviations.api.dto.DeviationMapper;
import backend.fullstack.deviations.api.dto.DeviationRequest;
import backend.fullstack.deviations.api.dto.DeviationResponse;
import backend.fullstack.deviations.api.dto.ResolveDeviationRequest;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationModuleType;
import backend.fullstack.deviations.domain.DeviationSeverity;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.deviations.infrastructure.DeviationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DeviationServiceTest {

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviationMapper deviationMapper;

    private DeviationService service;

    @BeforeEach
    void setUp() {
        service = new DeviationService(deviationRepository, organizationRepository, userRepository, deviationMapper);
    }

    @Test
    void getDeviations_noFilter_returnsAll() {
        Long orgId = 1L;
        Deviation d1 = buildDeviation(1L, orgId, DeviationStatus.OPEN);
        Deviation d2 = buildDeviation(2L, orgId, DeviationStatus.IN_PROGRESS);
        Deviation d3 = buildDeviation(3L, orgId, DeviationStatus.RESOLVED);

        when(deviationRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId))
                .thenReturn(List.of(d1, d2, d3));
        when(deviationMapper.toResponse(any(Deviation.class)))
                .thenAnswer(invocation -> buildResponse(((Deviation) invocation.getArgument(0)).getId()));

        List<DeviationResponse> result = service.getDeviations(orgId, null);

        assertEquals(3, result.size());
        verify(deviationRepository).findByOrganization_IdOrderByCreatedAtDesc(orgId);
    }

    @Test
    void getDeviations_withStatusFilter_callsFilteredRepo() {
        Long orgId = 1L;
        DeviationStatus filterStatus = DeviationStatus.OPEN;
        Deviation d1 = buildDeviation(1L, orgId, filterStatus);

        when(deviationRepository.findByOrganization_IdAndStatusOrderByCreatedAtDesc(orgId, filterStatus))
                .thenReturn(List.of(d1));
        when(deviationMapper.toResponse(any(Deviation.class)))
                .thenReturn(buildResponse(1L));

        List<DeviationResponse> result = service.getDeviations(orgId, filterStatus);

        assertEquals(1, result.size());
        verify(deviationRepository).findByOrganization_IdAndStatusOrderByCreatedAtDesc(orgId, filterStatus);
    }

    @Test
    void createDeviation_setsStatusOpen() {
        Long orgId = 1L;
        Long userId = 5L;

        Organization org = buildOrganization(orgId);
        User user = buildUser(userId, orgId);
        DeviationRequest request = new DeviationRequest(
                "Temp too high", "Freezer above threshold", DeviationSeverity.HIGH, DeviationModuleType.IK_MAT
        );
        DeviationResponse expectedResponse = buildResponse(10L);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(deviationRepository.save(any(Deviation.class)))
                .thenAnswer(invocation -> {
                    Deviation d = invocation.getArgument(0);
                    d = Deviation.builder()
                            .id(10L)
                            .organization(d.getOrganization())
                            .title(d.getTitle())
                            .description(d.getDescription())
                            .status(d.getStatus())
                            .severity(d.getSeverity())
                            .moduleType(d.getModuleType())
                            .reportedBy(d.getReportedBy())
                            .build();
                    return d;
                });
        when(deviationMapper.toResponse(any(Deviation.class))).thenReturn(expectedResponse);

        service.createDeviation(orgId, userId, request);

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());
        Deviation saved = captor.getValue();

        assertEquals(DeviationStatus.OPEN, saved.getStatus());
    }

    @Test
    void resolveDeviation_setsStatusResolved() {
        Long orgId = 1L;
        Long userId = 5L;
        Long deviationId = 20L;

        Organization org = buildOrganization(orgId);
        User resolver = buildUser(userId, orgId);
        Deviation existing = buildDeviation(deviationId, orgId, DeviationStatus.OPEN);
        existing.setOrganization(org);

        ResolveDeviationRequest request = new ResolveDeviationRequest("Fixed the freezer seal");
        DeviationResponse expectedResponse = buildResponse(deviationId);

        when(deviationRepository.findByIdAndOrganization_Id(deviationId, orgId))
                .thenReturn(Optional.of(existing));
        when(userRepository.findById(userId)).thenReturn(Optional.of(resolver));
        when(deviationRepository.save(any(Deviation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(deviationMapper.toResponse(any(Deviation.class))).thenReturn(expectedResponse);

        service.resolveDeviation(orgId, userId, deviationId, request);

        ArgumentCaptor<Deviation> captor = ArgumentCaptor.forClass(Deviation.class);
        verify(deviationRepository).save(captor.capture());
        Deviation saved = captor.getValue();

        assertEquals(DeviationStatus.RESOLVED, saved.getStatus());
        assertNotNull(saved.getResolvedAt());
        assertEquals(resolver, saved.getResolvedBy());
    }

        @Test
        void resolveDeviation_blankResolution_throwsIllegalArgumentException() {
                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.resolveDeviation(1L, 5L, 20L, new ResolveDeviationRequest("   "))
                );

                assertEquals("Resolution text is required when status is RESOLVED", ex.getMessage());
        }

    // --- helpers ---

    private static Organization buildOrganization(Long id) {
        return Organization.builder()
                .id(id)
                .name("Everest")
                .organizationNumber("123456789")
                .build();
    }

    private static User buildUser(Long id, Long orgId) {
        return User.builder()
                .id(id)
                .firstName("Per")
                .lastName("Olsen")
                .email("per@everestsushi.no")
                .passwordHash("hashed")
                .organization(buildOrganization(orgId))
                .build();
    }

    private static Deviation buildDeviation(Long id, Long orgId, DeviationStatus status) {
        Organization org = buildOrganization(orgId);
        User reporter = buildUser(99L, orgId);
        return Deviation.builder()
                .id(id)
                .organization(org)
                .title("Test deviation")
                .description("Some description")
                .status(status)
                .severity(DeviationSeverity.MEDIUM)
                .moduleType(DeviationModuleType.IK_MAT)
                .reportedBy(reporter)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static DeviationResponse buildResponse(Long id) {
        return new DeviationResponse(
                id,
                "Test deviation",
                "Some description",
                DeviationStatus.OPEN,
                DeviationSeverity.MEDIUM,
                DeviationModuleType.IK_MAT,
                "Per Olsen",
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }
}
