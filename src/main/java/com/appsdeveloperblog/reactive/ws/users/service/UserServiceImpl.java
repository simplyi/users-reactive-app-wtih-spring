package com.appsdeveloperblog.reactive.ws.users.service;

import com.appsdeveloperblog.reactive.ws.users.data.UserEntity;
import com.appsdeveloperblog.reactive.ws.users.data.UserRepository;
import com.appsdeveloperblog.reactive.ws.users.presentation.CreateUserRequest;
import com.appsdeveloperblog.reactive.ws.users.presentation.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono
                .mapNotNull(this::convertToEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToRest);
    }

    @Override
    public Mono<UserRest> getUserById(UUID id) {
        return userRepository
                .findById(id)
                .mapNotNull(userEntity -> convertToRest(userEntity));
    }

    private UserEntity convertToEntity(CreateUserRequest createUserRequest) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(createUserRequest, userEntity);
        return userEntity;
    }

    private UserRest convertToRest(UserEntity userEntity) {
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest);
        return userRest;
    }
}
