package backend.fullstack.training;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.exceptions.AccessDeniedException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.training.dto.TrainingRecordRequest;
import backend.fullstack.training.dto.TrainingRecordResponse;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

/**
 * Service for managing staff training records within an organization.
 */
@Service
public class TrainingService {

    private final TrainingRecordRepository trainingRecordRepository;
    private final UserRepository userRepository;
    private final AccessContextService accessContext;
    private final AuthorizationService authorizationService;

    public TrainingService(
            TrainingRecordRepository trainingRecordRepository,
            UserRepository userRepository,
            AccessContextService accessContext,
            AuthorizationService authorizationService
    ) {
        this.trainingRecordRepository = trainingRecordRepository;
        this.userRepository = userRepository;
        this.accessContext = accessContext;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public TrainingRecordResponse create(TrainingRecordRequest request) {
        User targetUser = findUserInCurrentOrganization(request.getUserId());
        authorizationService.assertCanManageUser(targetUser);
        validateRequestWindow(request);

        TrainingRecord trainingRecord = TrainingRecord.builder()
                .user(targetUser)
                .trainingType(request.getTrainingType())
                .status(request.getStatus())
                .completedAt(resolveCompletedAt(request))
                .expiresAt(request.getExpiresAt())
                .build();

        return toResponse(trainingRecordRepository.save(trainingRecord));
    }

    @Transactional(readOnly = true)
    public List<TrainingRecordResponse> getVisibleRecords(Long userId, TrainingType trainingType, TrainingStatus status) {
        Long organizationId = accessContext.getCurrentOrganizationId();

        if (userId != null) {
            User targetUser = findUserInCurrentOrganization(userId);
            assertCanViewTrainingForUser(targetUser);
        }

        return trainingRecordRepository.findVisibleRecords(organizationId, userId, trainingType, status)
                .stream()
                .filter(record -> canViewTrainingForUser(record.getUser()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainingRecordResponse getById(Long id) {
        TrainingRecord trainingRecord = findRecordInCurrentOrganization(id);
        assertCanViewTrainingForUser(trainingRecord.getUser());
        return toResponse(trainingRecord);
    }

    @Transactional
    public TrainingRecordResponse update(Long id, TrainingRecordRequest request) {
        TrainingRecord trainingRecord = findRecordInCurrentOrganization(id);
        User targetUser = findUserInCurrentOrganization(request.getUserId());
        authorizationService.assertCanManageUser(targetUser);
        validateRequestWindow(request);

        trainingRecord.setUser(targetUser);
        trainingRecord.setTrainingType(request.getTrainingType());
        trainingRecord.setStatus(request.getStatus());
        trainingRecord.setCompletedAt(resolveCompletedAt(request));
        trainingRecord.setExpiresAt(request.getExpiresAt());

        return toResponse(trainingRecordRepository.save(trainingRecord));
    }

    private TrainingRecord findRecordInCurrentOrganization(Long id) {
        Long organizationId = accessContext.getCurrentOrganizationId();
        return trainingRecordRepository.findByIdAndUser_Organization_Id(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found"));
    }

    private User findUserInCurrentOrganization(Long userId) {
        Long organizationId = accessContext.getCurrentOrganizationId();
        return userRepository.findById(userId)
                .filter(user -> organizationId.equals(user.getOrganizationId()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateRequestWindow(TrainingRecordRequest request) {
        if (request.getExpiresAt() != null && request.getCompletedAt() != null
                && request.getExpiresAt().isBefore(request.getCompletedAt())) {
            throw new RoleException("expiresAt must be after completedAt");
        }
    }

    private LocalDateTime resolveCompletedAt(TrainingRecordRequest request) {
        if (request.getStatus() == TrainingStatus.COMPLETED) {
            return request.getCompletedAt() != null ? request.getCompletedAt() : LocalDateTime.now();
        }
        return request.getCompletedAt();
    }

    private void assertCanViewTrainingForUser(User targetUser) {
        if (!canViewTrainingForUser(targetUser)) {
            throw new AccessDeniedException("No access to this training record");
        }
    }

    private boolean canViewTrainingForUser(User targetUser) {
        User actor = accessContext.getCurrentUser();
        return actor.getId().equals(targetUser.getId()) || authorizationService.canViewUser(targetUser);
    }

    private TrainingRecordResponse toResponse(TrainingRecord trainingRecord) {
        User user = trainingRecord.getUser();
        return TrainingRecordResponse.builder()
                .id(trainingRecord.getId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .organizationId(user.getOrganizationId())
                .trainingType(trainingRecord.getTrainingType())
                .status(trainingRecord.getStatus())
                .completedAt(trainingRecord.getCompletedAt())
                .expiresAt(trainingRecord.getExpiresAt())
                .build();
    }
}
