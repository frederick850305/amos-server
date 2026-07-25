package com.neusoft.amos.system.web;

import com.neusoft.amos.common.ApiResponse;
import com.neusoft.amos.system.dto.LoginRequest;
import com.neusoft.amos.system.dto.LoginResponse;
import com.neusoft.amos.system.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ApiResponse.fail("username and password required");
        }
        try {
            return ApiResponse.ok(authService.login(request.username(), request.password()));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
