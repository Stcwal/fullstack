package backend.fullstack.training;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import backend.fullstack.organization.Organization;
import backend.fullstack.user.User;
import backend.fullstack.user.role.Role;

@DataJpaTest
class TrainingRecordRepositoryTest {

    @Autowired
    private TrainingRecordRepository trainingRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void hasValidTrainingReturnsTrueForCompletedNonExpiredRecord() {
        User user = persistUser("manager@everest.no");

        entityManager.persist(TrainingRecord.builder()
                .user(user)
                .trainingType(TrainingType.CHECKLIST_APPROVAL)
                .status(TrainingStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build());
        entityManager.flush();

        boolean hasValidTraining = trainingRecordRepository.hasValidTraining(
                user.getId(),
                TrainingType.CHECKLIST_APPROVAL,
                LocalDateTime.now()
        );

        assertTrue(hasValidTraining);
    }

    @Test
    void hasValidTrainingReturnsFalseForExpiredRecord() {
        User user = persistUser("staff@everest.no");

        entityManager.persist(TrainingRecord.builder()
                .user(user)
                .trainingType(TrainingType.FREEZER_LOGGING)
                .status(TrainingStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusDays(30))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build());
        entityManager.flush();

        boolean hasValidTraining = trainingRecordRepository.hasValidTraining(
                user.getId(),
                TrainingType.FREEZER_LOGGING,
                LocalDateTime.now()
        );

        assertFalse(hasValidTraining);
    }

    @Test
    void findVisibleRecordsFiltersByOrganizationAndOptionalFields() {
        Organization organization = persistOrganization("Everest", "937219997");
        Organization otherOrganization = persistOrganization("Other", "123456789");

        User user = persistUser(organization, "staff@everest.no");
        User otherUser = persistUser(otherOrganization, "staff@other.no");

        entityManager.persist(TrainingRecord.builder()
                .user(user)
                .trainingType(TrainingType.GENERAL)
                .status(TrainingStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build());

        entityManager.persist(TrainingRecord.builder()
                .user(otherUser)
                .trainingType(TrainingType.GENERAL)
                .status(TrainingStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build());

        entityManager.flush();

        List<TrainingRecord> records = trainingRecordRepository.findVisibleRecords(
                organization.getId(),
                user.getId(),
                TrainingType.GENERAL,
                TrainingStatus.COMPLETED
        );

        assertTrue(records.stream().allMatch(record -> record.getUser().getOrganizationId().equals(organization.getId())));
        assertTrue(records.stream().allMatch(record -> record.getUser().getId().equals(user.getId())));
        assertTrue(records.stream().allMatch(record -> record.getTrainingType() == TrainingType.GENERAL));
    }

    private User persistUser(String email) {
        Organization organization = persistOrganization("Everest", email.hashCode() % 2 == 0 ? "937219998" : "937219999");
        return persistUser(organization, email);
    }

    private User persistUser(Organization organization, String email) {
        User user = User.builder()
                .organization(organization)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .passwordHash("hash")
                .role(Role.STAFF)
                .isActive(true)
                .build();
        entityManager.persist(user);
        return user;
    }

    private Organization persistOrganization(String name, String organizationNumber) {
        Organization organization = Organization.builder()
                .name(name)
                .organizationNumber(organizationNumber)
                .build();
        entityManager.persist(organization);
        return organization;
    }
}
