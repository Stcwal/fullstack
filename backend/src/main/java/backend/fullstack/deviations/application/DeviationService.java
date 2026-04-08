package backend.fullstack.deviations.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.deviations.api.dto.DeviationCommentRequest;
import backend.fullstack.deviations.api.dto.DeviationCommentResponse;
import backend.fullstack.deviations.api.dto.DeviationDetailsResponse;
import backend.fullstack.deviations.api.dto.DeviationMapper;
import backend.fullstack.deviations.api.dto.DeviationRequest;
import backend.fullstack.deviations.api.dto.DeviationResponse;
import backend.fullstack.deviations.api.dto.ResolveDeviationRequest;
import backend.fullstack.deviations.api.dto.UpdateDeviationStatusRequest;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationComment;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.deviations.infrastructure.DeviationCommentRepository;
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
    private final DeviationCommentRepository deviationCommentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DeviationMapper deviationMapper;

    public DeviationService(
            DeviationRepository deviationRepository,
            DeviationCommentRepository deviationCommentRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            DeviationMapper deviationMapper
    ) {
        this.deviationRepository = deviationRepository;
        this.deviationCommentRepository = deviationCommentRepository;
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

            @Transactional(readOnly = true)
            public DeviationDetailsResponse getDeviationById(Long organizationId, Long deviationId) {
            Deviation deviation = deviationRepository.findByIdAndOrganization_Id(deviationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Deviation", deviationId));

            List<DeviationCommentResponse> comments = deviationCommentRepository
                .findByOrganization_IdAndDeviation_IdOrderByCreatedAtAsc(organizationId, deviationId)
                .stream()
                .map(comment -> new DeviationCommentResponse(
                    comment.getId(),
                    comment.getCommentText(),
                    comment.getCreatedById(),
                    comment.getCreatedByName(),
                    comment.getCreatedAt()
                ))
                .toList();

            DeviationResponse base = deviationMapper.toResponse(deviation);
            return new DeviationDetailsResponse(
                base.id(),
                base.title(),
                base.description(),
                base.status(),
                base.severity(),
                base.moduleType(),
                base.reportedBy(),
                base.reportedAt(),
                base.resolvedBy(),
                base.resolvedAt(),
                base.resolution(),
                deviation.getRelatedReadingId(),
                comments
            );
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

        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        assertUserInOrganization(actor, organizationId);
        assertAllowedTransition(deviation.getStatus(), request.status());

        deviation.setStatus(request.status());

        if (request.status() == DeviationStatus.RESOLVED) {
            deviation.setResolvedBy(actor);
            deviation.setResolvedAt(LocalDateTime.now());
            deviation.setResolution(request.resolution());
        } else {
            deviation.setResolvedBy(null);
            deviation.setResolvedAt(null);
            deviation.setResolution(null);
        }

        Deviation saved = deviationRepository.save(deviation);
        return deviationMapper.toResponse(saved);
    }

    private void validateResolutionForResolvedStatus(String resolution) {
        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException("Resolution text is required when status is RESOLVED");
        }
    }
}
