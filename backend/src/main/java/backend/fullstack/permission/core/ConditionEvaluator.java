package backend.fullstack.permission.core;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import backend.fullstack.permission.model.Permission;
import backend.fullstack.training.TrainingRecordRepository;
import backend.fullstack.training.TrainingType;
import backend.fullstack.user.User;

/**
 * Service responsible for evaluating permission conditions based on user attributes and contextual information. This service is used to determine whether specific conditions associated with a permission are satisfied for a given user.
 * 
 * The ConditionEvaluator checks various factors such as training completion, approval requirements, and other contextual data to ensure that permissions are granted only when all necessary conditions are met.
 *
 * @version 1.0
 * @since 31.03.26
 */
@Service
public class ConditionEvaluator {

    private final TrainingRecordRepository trainingRecordRepository;

    public ConditionEvaluator(TrainingRecordRepository trainingRecordRepository) {
        this.trainingRecordRepository = trainingRecordRepository;
    }

    /**
     * Evaluates whether the conditions associated with a given permission are satisfied for a specific user and context.
     *
     * @param user the user for whom the permission is being evaluated
     * @param permission the permission being evaluated
     * @param context the contextual information relevant to the permission evaluation
     * @return true if all conditions for the permission are satisfied, false otherwise
     */
    public boolean isConditionSatisfied(User user, Permission permission, PermissionConditionContext context) {
        if (user == null || permission == null) {
            return false;
        }

        PermissionConditionContext effectiveContext = context == null
                ? PermissionConditionContext.empty()
                : context;

        if (permission == Permission.LOGS_FREEZER_CREATE) {
            return effectiveContext.trainingCompleted()
                    && hasValidTraining(user.getId(), TrainingType.FREEZER_LOGGING);
        }

        if (permission == Permission.CHECKLISTS_APPROVE) {
            if (effectiveContext.approvalRequired() && !effectiveContext.trainingCompleted()) {
                return false;
            }
            return hasValidTraining(user.getId(), TrainingType.CHECKLIST_APPROVAL);
        }

        return true;
    }

    /** 
     * Helper method to check if the user has a valid training record for the specified training type. This method queries the TrainingRecordRepository to determine if there is an active training record that satisfies the requirements for the permission condition.
     *
     * @param userId the ID of the user whose training records are being checked
     * @param trainingType the type of training being checked
     * @return true if the user has a valid training record for the specified training type, false otherwise
     */
    private boolean hasValidTraining(Long userId, TrainingType trainingType) {
        if (userId == null) {
            return false;
        }

        try {
            return trainingRecordRepository.hasValidTraining(userId, trainingType, LocalDateTime.now());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
