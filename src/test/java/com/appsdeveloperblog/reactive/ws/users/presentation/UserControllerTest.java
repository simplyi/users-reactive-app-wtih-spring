package com.appsdeveloperblog.reactive.ws.users.presentation;

import com.appsdeveloperblog.reactive.ws.users.presentation.model.CreateUserRequest;
import com.appsdeveloperblog.reactive.ws.users.presentation.model.UserRest;
import com.appsdeveloperblog.reactive.ws.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(UserController.class)
public class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCreateUser_withValidRequest_returnsCreatedStatusAndUserDetails() {

        // Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "Sergey",
                "Kargopolov",
                "user@example.com",
                "123456789"
        );
        UUID userId = UUID.randomUUID();
        String expectedLocation = "/users/" + userId;
        UserRest expectedUserRest = new UserRest(
                userId,
                createUserRequest.getFirstName(),
                createUserRequest.getLastName(),
                createUserRequest.getEmail(),
                null
        );

        // Act
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(expectedLocation)
                .expectBody(UserRest.class)
                .value((response)->{
                     assertEquals(expectedUserRest.getId(), response.getId());
                     assertEquals(expectedUserRest.getFirstName(), response.getFirstName());
                     assertEquals(expectedUserRest.getLastName(), response.getLastName());
                     assertEquals(expectedUserRest.getEmail(), response.getEmail());
                });
        
        // Assert

    }

}
