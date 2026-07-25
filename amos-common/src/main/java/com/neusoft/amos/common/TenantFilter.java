package com.neusoft.amos.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从请求头提取 installation / department 写入 {@link TenantContext}。
 * 预留：后续由 Gateway / Auth 注入，前端开发期可用 X-Installation 模拟某艘船。
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            TenantContext.set(request.getHeader("X-Installation"), request.getHeader("X-Department"));
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
