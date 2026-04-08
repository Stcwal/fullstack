package backend.fullstack.deviations.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.deviations.api.dto.DeviationMapper;
import backend.fullstack.deviations.api.dto.DeviationRequest;
import backend.fullstack.deviations.api.dto.DeviationResponse;
import backend.fullstack.deviations.api.dto.ResolveDeviationRequest;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.deviations.infrastructure.DeviationRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

@Service
@Transactional
public class DeviationService {

    private final DeviationRepository deviationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DeviationMapper deviationMapper;

    public DeviationService(
            DeviationRepository deviationRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            DeviationMapper deviationMapper
    ) {
        this.deviationRepository = deviationRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.deviationMapper = deviationMapper;
    }

    @Transactional(readOnly = true)
    public List<DeviationResponse> getDeviations(Long organizationId, DeviationStatus status) {
        List<Deviation> deviations;
        if (status != null) {
            deviations = deviationRepository.findByOrganization_IdAndStatusOrderByCreatedAtDesc(organizationId, status);
        } else {
            deviations = deviationRepository.findByOrganization_IdOrderByCreatedAtDesc(organizationId);
        }
        return deviations.stream()
                .map(deviationMapper::toResponse)
                .toList();
    }

    public DeviationResponse createDeviation(Long organizationId, Long userId, DeviationRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Deviation deviation = Deviation.builder()
                .organization(organization)
                .title(request.title())
                .description(request.description())
                .status(DeviationStatus.OPEN)
                .severity(request.severity())
                .moduleType(request.moduleType())
                .reportedBy(reporter)
                .build();

        Deviation saved = deviationRepository.save(deviation);
        return deviationMapper.toResponse(saved);
    }

    public DeviationResponse resolveDeviation(Long organizationId, Long userId, Long deviationId, ResolveDeviationRequest request) {
        validateResolutionForResolvedStatus(request.resolution());

        Deviation deviation = deviationRepository.findByIdAndOrganization_Id(deviationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", deviationId));

        User resolver = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        deviation.setStatus(DeviationStatus.RESOLVED);
        deviation.setResolvedBy(resolver);
        deviation.setResolvedAt(LocalDateTime.now());
        deviation.setResolution(request.resolution());

        Deviation saved = deviationRepository.save(deviation);
        return deviationMapper.toResponse(saved);
    }

    private void validateResolutionForResolvedStatus(String resolution) {
        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException("Resolution text is required when status is RESOLVED");
        }
    }
}
