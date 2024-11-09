package com.appsdeveloperblog.reactive.ws.users.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users") //   http://localhost:8080/users
public class UserController {

    @PostMapping
    public Mono<ResponseEntity<UserRest>> createUser(@RequestBody @Valid Mono<CreateUserRequest> createUserRequest) {
        return createUserRequest.map(request -> new UserRest(UUID.randomUUID(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail())
        ).map(userRest -> ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/users/" + userRest.getId()))
                .body(userRest));
    }

    @GetMapping("/{userId}")
    public Mono<UserRest> getUser(@PathVariable("userId") UUID userId) {
        return Mono.just(new UserRest(
                userId,
                "Sergey",
                "Kargopolov",
                "test@test.com"
        ));
    }

    @GetMapping
    public Flux<UserRest> getUsers() {
        return Flux.just(
                new UserRest(UUID.randomUUID(), "Sergey", "Kargopolov", "test@test.com"),
                new UserRest(UUID.randomUUID(), "Alice", "Smith", "alice@test.com"),
                new UserRest(UUID.randomUUID(), "Bob", "Johnson", "bob@test.com")
        );
    }
}
