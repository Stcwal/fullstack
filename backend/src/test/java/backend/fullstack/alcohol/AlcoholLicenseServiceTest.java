package backend.fullstack.alcohol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.alcohol.api.dto.AlcoholLicenseRequest;
import backend.fullstack.alcohol.api.dto.AlcoholLicenseResponse;
import backend.fullstack.alcohol.application.AlcoholLicenseService;
import backend.fullstack.alcohol.domain.AlcoholLicense;
import backend.fullstack.alcohol.domain.LicenseType;
import backend.fullstack.alcohol.infrastructure.AlcoholLicenseRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;

@ExtendWith(MockitoExtension.class)
class AlcoholLicenseServiceTest {

    @Mock
    private AlcoholLicenseRepository licenseRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private AccessContextService accessContext;

    private AlcoholLicenseService licenseService;

    private static final Long ORG_ID = 100L;

    @BeforeEach
    void setUp() {
        licenseService = new AlcoholLicenseService(licenseRepository, organizationRepository, accessContext);
    }

    @Test
    void createLicenseSavesWithCorrectFields() {
        Organization org = organization();
        AlcoholLicenseRequest request = licenseRequest();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));
        when(licenseRepository.save(any(AlcoholLicense.class))).thenAnswer(invocation -> {
            AlcoholLicense license = invocation.getArgument(0);
            license.setId(1L);
            return license;
        });

        AlcoholLicenseResponse response = licenseService.create(request);

        ArgumentCaptor<AlcoholLicense> captor = ArgumentCaptor.forClass(AlcoholLicense.class);
        verify(licenseRepository).save(captor.capture());
        assertEquals(LicenseType.FULL_LICENSE, captor.getValue().getLicenseType());
        assertEquals("OSL-2026-1234", captor.getValue().getLicenseNumber());
        assertEquals(1L, response.getId());
    }

    @Test
    void listLicensesReturnsAllForOrganization() {
        AlcoholLicense license = AlcoholLicense.builder()
                .id(1L)
                .organization(organization())
                .licenseType(LicenseType.FULL_LICENSE)
                .licenseNumber("OSL-2026-1234")
                .expiresAt(LocalDate.of(2030, 1, 15))
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(licenseRepository.findByOrganization_IdOrderByExpiresAtDesc(ORG_ID)).thenReturn(List.of(license));

        List<AlcoholLicenseResponse> result = licenseService.listLicenses();

        assertEquals(1, result.size());
        assertEquals("OSL-2026-1234", result.get(0).getLicenseNumber());
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(licenseRepository.findByIdAndOrganization_Id(999L, ORG_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> licenseService.getById(999L));
    }

    @Test
    void updateLicenseModifiesFields() {
        AlcoholLicense existing = AlcoholLicense.builder()
                .id(1L)
                .organization(organization())
                .licenseType(LicenseType.BEER_WINE)
                .build();
        AlcoholLicenseRequest request = licenseRequest();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(licenseRepository.findByIdAndOrganization_Id(1L, ORG_ID)).thenReturn(Optional.of(existing));
        when(licenseRepository.save(any(AlcoholLicense.class))).thenAnswer(i -> i.getArgument(0));

        AlcoholLicenseResponse response = licenseService.update(1L, request);

        assertEquals(LicenseType.FULL_LICENSE, response.getLicenseType());
    }

    @Test
    void deleteLicenseRemovesEntity() {
        AlcoholLicense existing = AlcoholLicense.builder()
                .id(1L)
                .organization(organization())
                .licenseType(LicenseType.FULL_LICENSE)
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(licenseRepository.findByIdAndOrganization_Id(1L, ORG_ID)).thenReturn(Optional.of(existing));

        licenseService.delete(1L);

        verify(licenseRepository).delete(existing);
    }

    @Test
    void expiredLicenseIsMarkedAsExpired() {
        AlcoholLicense license = AlcoholLicense.builder()
                .id(1L)
                .organization(organization())
                .licenseType(LicenseType.FULL_LICENSE)
                .expiresAt(LocalDate.of(2020, 1, 1))
                .build();

        when(accessContext.getCurrentOrganizationId()).thenReturn(ORG_ID);
        when(licenseRepository.findByIdAndOrganization_Id(1L, ORG_ID)).thenReturn(Optional.of(license));

        AlcoholLicenseResponse response = licenseService.getById(1L);

        assertTrue(response.isExpired());
    }

    private static AlcoholLicenseRequest licenseRequest() {
        AlcoholLicenseRequest request = new AlcoholLicenseRequest();
        request.setLicenseType(LicenseType.FULL_LICENSE);
        request.setLicenseNumber("OSL-2026-1234");
        request.setIssuedAt(LocalDate.of(2026, 1, 15));
        request.setExpiresAt(LocalDate.of(2030, 1, 15));
        request.setIssuingAuthority("Oslo kommune");
        return request;
    }

    private static Organization organization() {
        return Organization.builder()
                .id(ORG_ID)
                .name("Everest Sushi & Fusion")
                .organizationNumber("937219997")
                .build();
    }
}
