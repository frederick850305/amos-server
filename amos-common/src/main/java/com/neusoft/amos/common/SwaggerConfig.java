package com.neusoft.amos.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置：访问 /swagger-ui.html。
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI amosOpenApi() {
        return new OpenAPI().info(
                new Info().title("AMOS M&P API").description("船舶维护 / 库存 / 采购 / 预算").version("v1"));
    }
}
