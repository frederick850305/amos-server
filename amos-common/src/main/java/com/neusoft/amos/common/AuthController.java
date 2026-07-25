package com.neusoft.amos.common;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 开发期登录端点：用 username 换取 JWT。
 * 配合 amos.security.enabled=true 时前端联调鉴权使用；未启用安全时也始终可用。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody Map<String, String> body) {
        String username = body == null ? null : body.get("username");
        if (username == null || username.isBlank()) {
            return ApiResponse.fail("username required");
        }
        return ApiResponse.ok(jwtUtil.generate(username));
    }
}
