package com.dynamiccarsharing.user.repository;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.model.UserReview;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(statements = {
        "DELETE FROM user_reviews",
        "DELETE FROM users",
        "DELETE FROM contact_infos",
        "ALTER SEQUENCE contact_info_seq RESTART WITH 1",
        "ALTER SEQUENCE user_seq RESTART WITH 1",
        "ALTER SEQUENCE user_review_seq RESTART WITH 1"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JpaLifecycleTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserReviewRepository userReviewRepository;

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private long count(Iterable<?> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).count();
    }

    private ContactInfo createTransientContactInfo() {
        return ContactInfo.builder()
                .firstName("Test")
                .lastName("User")
                .email(System.nanoTime() + "@example.com")
                .phoneNumber("123456789")
                .build();
    }

    private User createTransientUser() {
        return User.builder()
                .contactInfo(createTransientContactInfo())
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private UserReview createTransientReview(User parent, User reviewer) {
        return UserReview.builder().comment("A great user!").user(parent).reviewer(reviewer).build();
    }

    @Test
    @DisplayName("3.1: repository.save() on new entity should INSERT")
    void saveParentWithoutId_usingRepositorySave_shouldInsert() {
        User savedParent = userRepository.save(createTransientUser());
        assertNotNull(savedParent.getId());
    }

    @Test
    @DisplayName("3.2: entityManager.persist() on new entity should INSERT")
    void saveParentWithoutId_usingEntityManagerPersist_shouldInsert() {
        User parent = createTransientUser();
        transactionTemplate.executeWithoutResult(status -> entityManager.persist(parent));
        assertNotNull(parent.getId());
    }

    @Test
    @DisplayName("3.3: entityManager.merge() on new entity should INSERT")
    void saveParentWithoutId_usingEntityManagerMerge_shouldInsert() {
        User parent = createTransientUser();
        User mergedParent = transactionTemplate.execute(status -> entityManager.merge(parent));
        assert mergedParent != null;
        assertNotNull(mergedParent.getId());
    }

    @Test
    @DisplayName("4.1: repository.save() with non-existent ID should INSERT")
    void saveParentWithInitializedId_usingRepositorySave_shouldInsert() {
        User parent = createTransientUser().toBuilder().id(12345L).build();
        User savedParent = userRepository.save(parent);
        assertTrue(userRepository.findById(savedParent.getId()).isPresent());
    }

    @Test
    @DisplayName("4.2: entityManager.persist() with non-existent ID should FAIL")
    void saveParentWithInitializedId_usingEntityManagerPersist_shouldThrowException() {
        User parent = createTransientUser().toBuilder().id(12345L).build();
        assertThrows(PersistenceException.class, () -> transactionTemplate.executeWithoutResult(status -> entityManager.persist(parent)));
    }

    @Test
    @DisplayName("4.3: entityManager.merge() with non-existent ID should INSERT")
    void saveParentWithInitializedId_usingEntityManagerMerge_shouldInsert() {
        User parent = createTransientUser().toBuilder().id(12345L).build();
        User mergedParent = transactionTemplate.execute(status -> entityManager.merge(parent));
        assert mergedParent != null;
        assertTrue(userRepository.findById(mergedParent.getId()).isPresent());
    }

    @Test
    @DisplayName("5.1: repository.save() with existing ID should UPDATE")
    void saveParentWithConflictingId_usingRepositorySave_shouldUpdate() {
        User existingUser = userRepository.save(createTransientUser());
        entityManager.flush();
        entityManager.clear();

        User conflictingUser = User.builder()
                .id(existingUser.getId())
                .contactInfo(existingUser.getContactInfo())
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(conflictingUser);
        entityManager.flush();

        User updatedUser = userRepository.findById(existingUser.getId()).get();
        assertEquals(UserRole.ADMIN, updatedUser.getRole());
    }

    @Test
    @DisplayName("5.2: entityManager.persist() with existing ID should FAIL")
    void saveParentWithConflictingId_usingEntityManagerPersist_shouldThrowException() {
        User savedUser = userRepository.save(createTransientUser());
        entityManager.flush();
        User existingUser = userRepository.findById(savedUser.getId()).get();
        entityManager.clear();

        User conflictingUser = User.builder()
                .id(existingUser.getId())
                .contactInfo(existingUser.getContactInfo())
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        assertThrows(PersistenceException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(conflictingUser);
            });
        });
    }

    @Test
    @DisplayName("5.3: entityManager.merge() with existing ID should UPDATE")
    void saveParentWithConflictingId_usingEntityManagerMerge_shouldUpdate() {
        User existingUser = userRepository.save(createTransientUser());
        entityManager.flush();
        entityManager.clear();

        User conflictingUser = User.builder()
                .id(existingUser.getId())
                .contactInfo(existingUser.getContactInfo())
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        transactionTemplate.executeWithoutResult(status -> {
            entityManager.merge(conflictingUser);
        });
        entityManager.flush();

        User updatedUser = userRepository.findById(existingUser.getId()).get();
        assertEquals(UserRole.ADMIN, updatedUser.getRole());
    }

    @Test
    @DisplayName("6.1: save() on Parent with new Child should NOT cascade")
    void saveParentWithNewChild_usingRepositorySave_shouldNotCascade() {
        User parent = createTransientUser();
        createTransientReview(parent, parent);
        userRepository.save(parent);

        assertEquals(1, count(userRepository.findAll()));
        assertEquals(0, count(userReviewRepository.findAll()));
    }

    @Test
    @DisplayName("6.2: persist() on Parent with new Child should NOT cascade")
    void saveParentWithNewChild_usingEntityManagerPersist_shouldNotCascade() {
        User parent = createTransientUser();
        createTransientReview(parent, parent);
        transactionTemplate.executeWithoutResult(status -> entityManager.persist(parent));

        assertEquals(1, count(userRepository.findAll()));
        assertEquals(0, count(userReviewRepository.findAll()));
    }

    @Test
    @DisplayName("6.3: merge() on Parent with new Child should NOT cascade")
    void saveParentWithNewChild_usingEntityManagerMerge_shouldNotCascade() {
        User parent = createTransientUser();
        createTransientReview(parent, parent);
        transactionTemplate.executeWithoutResult(status -> entityManager.merge(parent));

        assertEquals(1, count(userRepository.findAll()));
        assertEquals(0, count(userReviewRepository.findAll()));
    }

    @Test
    @DisplayName("7.1: save() on Parent with existing Child should succeed")
    void saveParentWithExistingChild_usingRepositorySave_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        userReviewRepository.save(createTransientReview(parent, parent));
        entityManager.flush();
        entityManager.clear();

        User fetchedParent = userRepository.findById(parent.getId()).get();
        fetchedParent.setStatus(UserStatus.SUSPENDED);
        userRepository.save(fetchedParent);
        entityManager.flush();

        assertEquals(1, count(userRepository.findAll()));
        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(UserStatus.SUSPENDED, userRepository.findById(parent.getId()).get().getStatus());
    }

    @Test
    @DisplayName("7.2: persist() on a managed Parent entity should be ignored")
    void saveParentWithExistingChild_usingEntityManagerPersist_shouldBeIgnored() {
        User parent = userRepository.save(createTransientUser());
        userReviewRepository.save(createTransientReview(parent, parent));
        entityManager.flush();
        entityManager.clear();

        User fetchedParent = userRepository.findById(parent.getId()).get();

        assertDoesNotThrow(() -> {
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(fetchedParent));
        });
    }

    @Test
    @DisplayName("7.3: merge() on detached Parent with existing Child should succeed")
    void saveParentWithExistingChild_usingEntityManagerMerge_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        userReviewRepository.save(createTransientReview(parent, parent));
        entityManager.flush();
        entityManager.clear();

        User fetchedParent = userRepository.findById(parent.getId()).get();
        transactionTemplate.executeWithoutResult(status -> entityManager.merge(fetchedParent));

        assertEquals(1, count(userRepository.findAll()));
        assertEquals(1, count(userReviewRepository.findAll()));
    }

    @Test
    @DisplayName("8.1: save() Child without Parent should FAIL")
    void saveChildWithoutParent_usingRepositorySave_shouldThrowException() {
        User reviewer = userRepository.save(createTransientUser());
        UserReview child = UserReview.builder().comment("No parent user").reviewer(reviewer).build();

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            userReviewRepository.save(child);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("8.2: persist() Child without Parent should FAIL")
    void saveChildWithoutParent_usingEntityManagerPersist_shouldThrowException() {
        User reviewer = userRepository.save(createTransientUser());
        UserReview child = UserReview.builder().comment("No parent user").reviewer(reviewer).build();

        assertThrows(org.hibernate.PropertyValueException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(child);
                entityManager.flush();
            });
        });
    }

    @Test
    @DisplayName("8.3: merge() Child without Parent should FAIL")
    void saveChildWithoutParent_usingEntityManagerMerge_shouldThrowException() {
        User reviewer = userRepository.save(createTransientUser());
        UserReview child = UserReview.builder().comment("No parent").reviewer(reviewer).build();

        assertThrows(org.hibernate.PropertyValueException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.merge(child);
                entityManager.flush();
            });
        });
    }

    @Test
    @DisplayName("9.1: save() Child with transient Parent should FAIL")
    void saveChildWithTransientParent_usingRepositorySave_shouldThrowException() {
        User transientParent = createTransientUser();
        UserReview child = createTransientReview(transientParent, transientParent);

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            userReviewRepository.save(child);
            entityManager.flush();
        });

        entityManager.clear();
    }

    @Test
    @DisplayName("9.2: persist() Child with transient Parent should FAIL")
    void saveChildWithTransientParent_usingEntityManagerPersist_shouldThrowException() {
        User transientParent = createTransientUser();
        UserReview child = createTransientReview(transientParent, transientParent);

        assertThrows(IllegalStateException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                entityManager.persist(child);
                entityManager.flush();
            });
        });

        entityManager.clear();
    }

    @Test
    @DisplayName("9.3: merge() Child with transient Parent should FAIL")
    void saveChildWithTransientParent_usingEntityManagerMerge_shouldThrowException() {
        User transientParent = createTransientUser();
        UserReview child = createTransientReview(transientParent, transientParent);

        assertThrows(IllegalStateException.class, () -> {
            transactionTemplate.execute(status -> {
                UserReview mergedChild = entityManager.merge(child);
                entityManager.flush();
                return mergedChild;
            });
        });

        entityManager.clear();
    }

    @Test
    @DisplayName("10.1: save() Child with detached Parent should SUCCEED")
    void saveChildWithDetachedParent_usingRepositorySave_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        entityManager.flush();
        entityManager.clear();

        UserReview child = createTransientReview(parent, parent);
        userReviewRepository.save(child);
        entityManager.flush();

        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(parent.getId(), userReviewRepository.findAll().iterator().next().getUser().getId());
    }

    @Test
    @DisplayName("10.2: persist()/merge() Child with detached Parent should SUCCEED")
    void saveChildWithDetachedParent_usingEntityManager_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        entityManager.flush();
        entityManager.clear();

        UserReview child = createTransientReview(parent, parent);

        transactionTemplate.executeWithoutResult(status -> {
            entityManager.merge(child);
            entityManager.flush();
        });

        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(parent.getId(), userReviewRepository.findAll().iterator().next().getUser().getId());
    }

    @Test
    @DisplayName("10.3: merge() Child with detached Parent should SUCCEED")
    void saveChildWithDetachedParent_usingEntityManagerMerge_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        entityManager.flush();
        entityManager.clear();

        UserReview child = createTransientReview(parent, parent);
        transactionTemplate.executeWithoutResult(status -> entityManager.merge(child));
        entityManager.flush();

        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(parent.getId(), userReviewRepository.findAll().iterator().next().getUser().getId());
    }
}