package com.masterpiece.IPiece.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        
        SecurityScheme jwtAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("JWT", jwtAuthScheme))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("IPiece API Documentation")
                .description("""
                        IPiece STO Platform Backend API  
                        2차 거래(매칭 엔진), 보유자산, 호가조회 등 엔드포인트 문서
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("IPiece Server Team")
                        .email("support@ipiece.com"));
    }
}
