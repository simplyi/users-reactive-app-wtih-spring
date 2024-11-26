package com.appsdeveloperblog.reactive.ws.users.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Override
    public Mono<Map<String, String>> authenticate(String username, String password) {
        return null;
    }
}
