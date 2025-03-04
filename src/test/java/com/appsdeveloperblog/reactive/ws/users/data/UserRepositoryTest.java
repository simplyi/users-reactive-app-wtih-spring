package com.appsdeveloperblog.reactive.ws.users.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

@DataR2dbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setUp() {
        UserEntity user1 = new UserEntity(UUID.randomUUID(),
                "John",
                "Doe",
                "john.doe@example.com",
                "123456789");

        UserEntity user2 = new UserEntity(UUID.randomUUID(),
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "123456789");

        String insertSql = "INSERT INTO users (id, first_name, last_name, email, password) VALUES " +
                "(:id, :firstName, :lastName, :email, :password)";

        Flux.just(user1, user2)
                .concatMap(user -> databaseClient.sql(insertSql)
                        .bind("id", user.getId())
                        .bind("firstName", user.getFirstName())
                        .bind("lastName", user.getLastName())
                        .bind("email", user.getEmail())
                        .bind("password", user.getPassword())
                        .fetch()
                        .rowsUpdated())
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

    }

    @AfterAll
    void tearDown() {
        databaseClient.sql("TRUNCATE TABLE users")
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void testFindByEmail_WithEmailThatExists_ReturnsMatchingUser() {
        // Arrange
        String emailToFind = "john.doe@example.com";

        // Act and Assert
        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextMatches(user->user.getEmail().equals(emailToFind))
                .verifyComplete();
    }

    @Test
    void testFindByEmail_WithEmailThatDoesNotExist_ReturnsEmptyMono() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";

        // Act & Assert
        StepVerifier.create(userRepository.findByEmail(nonExistentEmail))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testFindAllBy_WithValidPageable_ReturnsPaginatedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2); // First page, page size = 2

        // Act & Assert
        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(2) // Expect exactly 2 items on the first page
                .verifyComplete();
    }

}