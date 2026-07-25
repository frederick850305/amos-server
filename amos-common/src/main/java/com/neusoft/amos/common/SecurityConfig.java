package com.neusoft.amos.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置。
 * amos.security.enabled=false（默认）：放开所有请求，便于联调；
 * amos.security.enabled=true：启用 JWT 校验，除登录 / 健康检查 / Swagger 外均需有效 Bearer Token。
 */
@Configuration
public class SecurityConfig {

    @Value("${amos.security.enabled:false}")
    private boolean securityEnabled;

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults());

        if (securityEnabled) {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/api/auth/**",
                        "/actuator/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                    ).permitAll()
                    .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }
        return http.build();
    }
}
