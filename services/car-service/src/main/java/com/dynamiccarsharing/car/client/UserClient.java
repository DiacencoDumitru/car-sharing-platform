package com.dynamiccarsharing.car.client;

import com.dynamiccarsharing.contracts.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/v1", configuration = UserClientFeignConfig.class)
public interface UserClient {
    @GetMapping("/internal/users/by-email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}
