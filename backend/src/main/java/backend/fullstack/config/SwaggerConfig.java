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
 * Configuration class for Swagger/OpenAPI documentation.
 * Sets up API metadata and security schemes for JWT authentication.
 *
 * @version 1.0
 * @since 20.04.2026
 */
@Configuration
public class SwaggerConfig {

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
                        .name("Gruppe 3- 4 life- fullstack ")
                        .email("stian@stian.stian"));
    }

    // This adds the "Authorize" button to Swagger UI
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Auth");
    }

    // This defines what "Bearer Auth" means — a JWT in the Authorization header
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