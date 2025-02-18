package com.appsdeveloperblog.reactive.ws.users.presentation;

import com.appsdeveloperblog.reactive.ws.users.infrastucture.TestSecurityConfig;
import com.appsdeveloperblog.reactive.ws.users.presentation.model.CreateUserRequest;
import com.appsdeveloperblog.reactive.ws.users.presentation.model.UserRest;
import com.appsdeveloperblog.reactive.ws.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(UserController.class)
@Import({TestSecurityConfig.class})
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

        when(userService.createUser(Mockito.<Mono<CreateUserRequest>>any())).thenReturn(Mono.just(expectedUserRest));

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
                .value((response) -> {
                    assertEquals(expectedUserRest.getId(), response.getId());
                    assertEquals(expectedUserRest.getFirstName(), response.getFirstName());
                    assertEquals(expectedUserRest.getLastName(), response.getLastName());
                    assertEquals(expectedUserRest.getEmail(), response.getEmail());
                });

        // Assert
        verify(userService, times(1)).createUser(Mockito.<Mono<CreateUserRequest>>any());

    }


    @Test
    void testCreateUser_withInvalidRequest_returnsBadRequest() {
        // Arrange
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "Sergey",
                "Kargopolov",
                "test@test.com",
                "123" // Short password
        );

        // Act & Assert
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).createUser(any());
    }

    @Test
    void testCreateUser_withEmptyFirstName_returnsBadRequest() {
        // Arrange
        CreateUserRequest invalidRequest = new CreateUserRequest(
                "", // Empty first name
                "Kargopolov",
                "user@example.com",
                "123456789"
        );

        // Act & Assert
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(userService, never()).createUser(any());
    }

    @Test
    void testCreateUser_whenServiceThrowsException_returnsInternalServerErrorWithExpectedStructure() {
        // Arrange
        CreateUserRequest validRequest = new CreateUserRequest(
                "Sergey",
                "Kargopolov",
                "user@example.com",
                "123456789"
        );

        when(userService.createUser(any())).thenReturn(Mono.error(new RuntimeException("Service error")));

        // Act & Assert
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.instance").isEqualTo("/users")
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.detail").isEqualTo("Service error");

        verify(userService, times(1)).createUser(any());
    }

}
