package com.appsdeveloperblog.reactive.ws.users.data;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    @Autowired
    private DatabaseClient databaseClient;

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
    void findByEmail() {
    }
}