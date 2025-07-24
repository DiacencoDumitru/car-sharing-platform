package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("jdbc")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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

    @AfterEach
    void tearDown() {
        userReviewRepository.findAll().forEach(review -> userReviewRepository.deleteById(review.getId()));
        userRepository.findAll().forEach(user -> userRepository.deleteById(user.getId()));

        contactInfoRepository.findAll().forEach(ci -> contactInfoRepository.deleteById(ci.getId()));
    }

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
        assertNotNull(userRepository.findById(savedParent.getId()));
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
        entityManager.clear();

        User conflictingUser = User.builder()
                .id(existingUser.getId())
                .contactInfo(existingUser.getContactInfo())
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(conflictingUser);

        User updatedUser = userRepository.findById(existingUser.getId()).get();
        assertEquals(UserRole.ADMIN, updatedUser.getRole());
    }

    @Test
    @DisplayName("5.2: entityManager.persist() with existing ID should FAIL")
    void saveParentWithConflictingId_usingEntityManagerPersist_shouldThrowException() {
        User savedUser = userRepository.save(createTransientUser());

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
        entityManager.clear();

        User fetchedParent = userRepository.findById(parent.getId()).get();
        userRepository.save(fetchedParent);

        assertEquals(1, count(userRepository.findAll()));
        assertEquals(1, count(userReviewRepository.findAll()));
    }

    @Test
    @DisplayName("7.2: persist() on Parent with existing Child should FAIL")
    void saveParentWithExistingChild_usingEntityManagerPersist_shouldFail() {
        User parent = userRepository.save(createTransientUser());
        userReviewRepository.save(createTransientReview(parent, parent));
        entityManager.clear();

        User fetchedParent = userRepository.findById(parent.getId()).get();
        assertThrows(PersistenceException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(fetchedParent));
        });
    }

    @Test
    @DisplayName("7.3: merge() on Parent with existing Child should succeed")
    void saveParentWithExistingChild_usingEntityManagerMerge_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        userReviewRepository.save(createTransientReview(parent, parent));
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
        UserReview child = UserReview.builder().comment("No parent").reviewer(reviewer).build();
        assertThrows(NullPointerException.class, () -> userReviewRepository.save(child));
    }

    @Test
    @DisplayName("8.2: persist() Child without Parent should FAIL")
    void saveChildWithoutParent_usingEntityManagerPersist_shouldThrowException() {
        User reviewer = userRepository.save(createTransientUser());
        UserReview child = UserReview.builder().comment("No parent").reviewer(reviewer).build();
        assertThrows(TransactionSystemException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(child));
        });
    }

    @Test
    @DisplayName("8.3: merge() Child without Parent should FAIL")
    void saveChildWithoutParent_usingEntityManagerMerge_shouldThrowException() {
        User reviewer = userRepository.save(createTransientUser());
        UserReview child = UserReview.builder().comment("No parent").reviewer(reviewer).build();
        assertThrows(TransactionSystemException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> entityManager.merge(child));
        });
    }

    @Test
    @DisplayName("9.1: save() Child with transient Parent should FAIL")
    void saveChildWithTransientParent_usingRepositorySave_shouldThrowException() {
        User transientParent = createTransientUser();
        UserReview child = createTransientReview(transientParent, transientParent);
        assertThrows(NullPointerException.class, () -> userReviewRepository.save(child));
    }

    @Test
    @DisplayName("9.2: persist() Child with transient Parent should FAIL")
    void saveChildWithTransientParent_usingEntityManagerPersist_shouldThrowException() {
        User transientParent = createTransientUser();
        UserReview child = createTransientReview(transientParent, transientParent);
        assertThrows(IllegalStateException.class, () -> {
            transactionTemplate.executeWithoutResult(status -> entityManager.persist(child));
        });
    }

    @Test
    @DisplayName("9.3: merge() Child with transient Parent should FAIL")
    void saveChildWithTransientParent_usingEntityManagerMerge_shouldThrowException() {
        User transientParent = createTransientUser();
        UserReview child = createTransientReview(transientParent, transientParent);
        assertThrows(IllegalStateException.class, () -> {
            transactionTemplate.execute(status -> entityManager.merge(child));
        });
    }

    @Test
    @DisplayName("10.1: save() Child with detached Parent should SUCCEED")
    void saveChildWithDetachedParent_usingRepositorySave_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        entityManager.clear();

        UserReview child = createTransientReview(parent, parent);
        userReviewRepository.save(child);

        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(parent.getId(), userReviewRepository.findAll().iterator().next().getUser().getId());
    }

    @Test
    @DisplayName("10.2: persist() Child with detached Parent should SUCCEED")
    void saveChildWithDetachedParent_usingEntityManagerPersist_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        entityManager.clear();

        UserReview child = createTransientReview(parent, parent);
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.persist(child);
            entityManager.flush();
        });

        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(parent.getId(), userReviewRepository.findAll().iterator().next().getUser().getId());
    }

    @Test
    @DisplayName("10.3: merge() Child with detached Parent should SUCCEED")
    void saveChildWithDetachedParent_usingEntityManagerMerge_shouldSucceed() {
        User parent = userRepository.save(createTransientUser());
        entityManager.clear();

        UserReview child = createTransientReview(parent, parent);
        transactionTemplate.executeWithoutResult(status -> entityManager.merge(child));

        assertEquals(1, count(userReviewRepository.findAll()));
        assertEquals(parent.getId(), userReviewRepository.findAll().iterator().next().getUser().getId());
    }
}