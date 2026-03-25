package com.dynamiccarsharing.notification.contacts;

import com.dynamiccarsharing.contracts.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContactServiceImpl implements UserContactService {

    private final WebClient.Builder webClientBuilder;

    @Override
    public Optional<String> getRenterEmail(Long renterId) {
        // user-service endpoints are accessible without ROLE_ADMIN for internal GET /users/{id}.
        UserDto user = webClientBuilder.baseUrl("lb://user-service")
                .build()
                .get()
                .uri("/api/v1/users/{userId}", renterId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> {
                    log.warn("Failed to fetch user contacts from user-service (status={}) renterId={}",
                            resp.statusCode(), renterId);
                    return resp.createException().flatMap(Mono::error);
                })
                .bodyToMono(UserDto.class)
                .block();

        if (user == null || user.getContactInfo() == null || user.getContactInfo().getEmail() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.getContactInfo().getEmail());
    }

    @Override
    public Optional<String> getRenterPhoneNumber(Long renterId) {
        UserDto user = webClientBuilder.baseUrl("lb://user-service")
                .build()
                .get()
                .uri("/api/v1/users/{userId}", renterId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> {
                    log.warn("Failed to fetch user contacts from user-service (status={}) renterId={}",
                            resp.statusCode(), renterId);
                    return resp.createException().flatMap(Mono::error);
                })
                .bodyToMono(UserDto.class)
                .block();

        if (user == null || user.getContactInfo() == null || user.getContactInfo().getPhoneNumber() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.getContactInfo().getPhoneNumber());
    }
}

