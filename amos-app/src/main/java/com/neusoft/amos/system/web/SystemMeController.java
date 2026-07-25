package com.neusoft.amos.system.web;

import com.neusoft.amos.common.JwtUtil;
import com.neusoft.amos.system.domain.AmosUser;
import com.neusoft.amos.system.dto.RoleDto;
import com.neusoft.amos.system.dto.ScopeResponse;
import com.neusoft.amos.system.dto.UserDto;
import com.neusoft.amos.system.dto.UserOptionDto;
import com.neusoft.amos.system.service.SystemScopeService;
import com.neusoft.amos.system.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemMeController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final SystemScopeService scopeService;

    public SystemMeController(JwtUtil jwtUtil, UserService userService, SystemScopeService scopeService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.scopeService = scopeService;
    }

    @GetMapping("/me")
    public UserDto me(HttpServletRequest request) {
        String username = usernameFrom(request);
        AmosUser user = userService.loadByUsername(username);
        List<RoleDto> roles = user.getRoles().stream()
                .map(r -> new RoleDto(r.getId(), r.getCode(), r.getName()))
                .toList();
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getStatus(), roles);
    }

    @GetMapping("/me/scopes")
    public ScopeResponse scopes(HttpServletRequest request) {
        return scopeService.getScopes(usernameFrom(request));
    }

    @GetMapping("/me/options")
    public List<UserOptionDto> options(HttpServletRequest request) {
        return scopeService.getOptions(usernameFrom(request));
    }

    @PutMapping("/me/options")
    public UserOptionDto setOption(HttpServletRequest request, @RequestBody UserOptionDto dto) {
        return scopeService.setOption(usernameFrom(request), dto.key(), dto.value());
    }

    private String usernameFrom(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing bearer token");
        }
        try {
            Claims claims = jwtUtil.parse(header.substring(7));
            String subject = claims.getSubject();
            if (subject == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
            }
            return subject;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }
    }
}
