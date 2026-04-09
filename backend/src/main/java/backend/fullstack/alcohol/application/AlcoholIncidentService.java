package backend.fullstack.alcohol.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.alcohol.api.dto.AlcoholIncidentRequest;
import backend.fullstack.alcohol.api.dto.AlcoholIncidentResponse;
import backend.fullstack.alcohol.api.dto.ResolveIncidentRequest;
import backend.fullstack.alcohol.domain.AlcoholServingIncident;
import backend.fullstack.alcohol.domain.IncidentStatus;
import backend.fullstack.alcohol.domain.IncidentType;
import backend.fullstack.alcohol.infrastructure.AlcoholIncidentRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;

/**
 * Service for managing alcohol-related serving incidents under IK-Alkohol compliance.
 */
@Service
public class AlcoholIncidentService {

    private final AlcoholIncidentRepository incidentRepository;
    private final OrganizationRepository organizationRepository;
    private final LocationRepository locationRepository;
    private final AccessContextService accessContext;

    public AlcoholIncidentService(
            AlcoholIncidentRepository incidentRepository,
            OrganizationRepository organizationRepository,
            LocationRepository locationRepository,
            AccessContextService accessContext
    ) {
        this.incidentRepository = incidentRepository;
        this.organizationRepository = organizationRepository;
        this.locationRepository = locationRepository;
        this.accessContext = accessContext;
    }

    @Transactional
    public AlcoholIncidentResponse create(AlcoholIncidentRequest request) {
        Long orgId = accessContext.getCurrentOrganizationId();
        User currentUser = accessContext.getCurrentUser();

        accessContext.assertCanAccess(request.getLocationId());

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        Location location = findLocationInOrganization(request.getLocationId(), orgId);

        AlcoholServingIncident incident = AlcoholServingIncident.builder()
                .organization(organization)
                .location(location)
                .reportedBy(currentUser)
                .incidentType(request.getIncidentType())
                .severity(request.getSeverity())
                .status(IncidentStatus.OPEN)
                .description(request.getDescription())
                .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now())
                .build();

        return toResponse(incidentRepository.save(incident));
    }

    @Transactional(readOnly = true)
    public List<AlcoholIncidentResponse> list(
            Long locationId,
            IncidentStatus status,
            IncidentType incidentType,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Long orgId = accessContext.getCurrentOrganizationId();
        return incidentRepository.findFiltered(orgId, locationId, status, incidentType, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlcoholIncidentResponse getById(Long id) {
        return toResponse(findInCurrentOrganization(id));
    }

    @Transactional
    public AlcoholIncidentResponse resolve(Long id, ResolveIncidentRequest request) {
        AlcoholServingIncident incident = findInCurrentOrganization(id);
        User currentUser = accessContext.getCurrentUser();

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setCorrectiveAction(request.getCorrectiveAction());
        incident.setResolvedBy(currentUser);
        incident.setResolvedAt(LocalDateTime.now());

        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public AlcoholIncidentResponse updateStatus(Long id, IncidentStatus newStatus) {
        AlcoholServingIncident incident = findInCurrentOrganization(id);
        incident.setStatus(newStatus);

        if (newStatus == IncidentStatus.CLOSED || newStatus == IncidentStatus.RESOLVED) {
            User currentUser = accessContext.getCurrentUser();
            if (incident.getResolvedBy() == null) {
                incident.setResolvedBy(currentUser);
            }
            if (incident.getResolvedAt() == null) {
                incident.setResolvedAt(LocalDateTime.now());
            }
        }

        return toResponse(incidentRepository.save(incident));
    }

    private AlcoholServingIncident findInCurrentOrganization(Long id) {
        Long orgId = accessContext.getCurrentOrganizationId();
        return incidentRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Alcohol incident not found"));
    }

    private Location findLocationInOrganization(Long locationId, Long orgId) {
        return locationRepository.findById(locationId)
                .filter(loc -> orgId.equals(loc.getOrganizationId()))
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    private AlcoholIncidentResponse toResponse(AlcoholServingIncident incident) {
        User reporter = incident.getReportedBy();
        User resolver = incident.getResolvedBy();
        Location location = incident.getLocation();

        return AlcoholIncidentResponse.builder()
                .id(incident.getId())
                .organizationId(incident.getOrganizationId())
                .locationId(incident.getLocationId())
                .locationName(location != null ? location.getName() : null)
                .reportedByUserId(reporter != null ? reporter.getId() : null)
                .reportedByName(reporter != null ? reporter.getFirstName() + " " + reporter.getLastName() : null)
                .resolvedByUserId(resolver != null ? resolver.getId() : null)
                .resolvedByName(resolver != null ? resolver.getFirstName() + " " + resolver.getLastName() : null)
                .incidentType(incident.getIncidentType())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .description(incident.getDescription())
                .correctiveAction(incident.getCorrectiveAction())
                .occurredAt(incident.getOccurredAt())
                .resolvedAt(incident.getResolvedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
