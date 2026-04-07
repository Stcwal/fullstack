package backend.fullstack.training;

import java.time.LocalDateTime;

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
