package backend.fullstack.training;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing TrainingRecord entities.
 * Provides methods to perform CRUD operations and custom queries related to training records for users.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {

    Optional<TrainingRecord> findByIdAndUser_Organization_Id(Long id, Long organizationId);

    @Query("""
        SELECT t
        FROM TrainingRecord t
        WHERE t.user.organization.id = :organizationId
          AND (:userId IS NULL OR t.user.id = :userId)
          AND (:trainingType IS NULL OR t.trainingType = :trainingType)
          AND (:status IS NULL OR t.status = :status)
        ORDER BY t.completedAt DESC NULLS LAST, t.id DESC
        """)
    List<TrainingRecord> findVisibleRecords(
            @Param("organizationId") Long organizationId,
            @Param("userId") Long userId,
            @Param("trainingType") TrainingType trainingType,
            @Param("status") TrainingStatus status
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM TrainingRecord t
        WHERE t.user.id = :userId
          AND t.trainingType = :trainingType
          AND t.status = backend.fullstack.training.TrainingStatus.COMPLETED
          AND (t.expiresAt IS NULL OR t.expiresAt >= :at)
        """)
    boolean hasValidTraining(
            @Param("userId") Long userId,
            @Param("trainingType") TrainingType trainingType,
            @Param("at") LocalDateTime at
    );
}
