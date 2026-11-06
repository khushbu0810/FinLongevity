package com.example.identity_service.controller;

import com.example.identity_service.dto.UserResponse;
import com.example.identity_service.entity.AppUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/users/me")
    public UserResponse currentUser(@AuthenticationPrincipal AppUser user) {
        return UserResponse.from(user);
    }
}
