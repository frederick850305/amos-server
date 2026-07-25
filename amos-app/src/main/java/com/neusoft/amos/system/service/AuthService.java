package com.neusoft.amos.system.service;

import com.neusoft.amos.common.JwtUtil;
import com.neusoft.amos.system.domain.AmosUser;
import com.neusoft.amos.system.dto.LoginResponse;
import com.neusoft.amos.system.dto.RoleDto;
import com.neusoft.amos.system.dto.ScopeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserService userService;
    private final SystemScopeService scopeService;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, SystemScopeService scopeService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.scopeService = scopeService;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String username, String password) {
        if (!userService.checkPassword(username, password)) {
            throw new IllegalArgumentException("invalid username or password");
        }
        AmosUser user = userService.loadByUsername(username);
        String token = jwtUtil.generate(username);
        List<RoleDto> roles = user.getRoles().stream()
                .map(r -> new RoleDto(r.getId(), r.getCode(), r.getName()))
                .toList();
        ScopeResponse scopes = scopeService.getScopes(username);
        return new LoginResponse(token, user.getUsername(), user.getDisplayName(), roles, scopes);
    }
}
