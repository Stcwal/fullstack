package backend.fullstack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures OpenAPI/Swagger metadata and JWT bearer authentication scheme.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Declares the OpenAPI model exposed by springdoc.
     *
     * @return OpenAPI definition for IK-Control backend
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement())
                .components(securityScheme());
    }

    private Info apiInfo() {
        return new Info()
                .title("IK-Control API")
                .description("Internal control system for restaurants and food/alcohol serving establishments.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("IK-Control Team")
                        .email("stian@stian.stian"));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Auth");
    }

    private Components securityScheme() {
        return new Components()
                .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                        .name("Bearer Auth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Paste your JWT token here. Get it from POST /api/auth/login"));
    }
}