package backend.fullstack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import backend.fullstack.user.role.Role;
import io.swagger.v3.oas.models.OpenAPI;

class ConfigSimpleComponentsTest {

    @Test
    void apiResponseConstructorsFactoriesAndSettersWork() {
        ApiResponse<String> empty = new ApiResponse<>();
        empty.setSuccess(true);
        empty.setMessage("ok");
        empty.setData("payload");

        assertTrue(empty.isSuccess());
        assertEquals("ok", empty.getMessage());
        assertEquals("payload", empty.getData());

        ApiResponse<String> constructed = new ApiResponse<>(false, "nope", "x");
        assertFalse(constructed.isSuccess());
        assertEquals("nope", constructed.getMessage());
        assertEquals("x", constructed.getData());

        ApiResponse<String> successDefault = ApiResponse.success("data");
        assertTrue(successDefault.isSuccess());
        assertEquals("Success", successDefault.getMessage());
        assertEquals("data", successDefault.getData());

        ApiResponse<Integer> successCustom = ApiResponse.success("created", 42);
        assertTrue(successCustom.isSuccess());
        assertEquals("created", successCustom.getMessage());
        assertEquals(42, successCustom.getData());

        ApiResponse<Void> error = ApiResponse.error("boom");
        assertFalse(error.isSuccess());
        assertEquals("boom", error.getMessage());
    }

    @Test
    void jwtPropertiesGettersSettersAndCompatibilityAccessorsWork() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789012345678901234567890123456789012345678901234567890123");
        properties.setExpirationMs(1234L);
        properties.setCookieName("auth");
        properties.setCookieSecure(false);

        assertEquals(properties.getSecret(), properties.secret());
        assertEquals(properties.getExpirationMs(), properties.expirationMs());
        assertEquals(properties.getCookieName(), properties.cookieName());
        assertEquals(properties.isCookieSecure(), properties.cookieSecure());
    }

    @Test
    void jwtPrincipalNormalizesAndDefensivelyCopiesLocationIds() {
        JwtPrincipal withNull = new JwtPrincipal(1L, "u@example.com", Role.ADMIN, 10L, null);
        assertTrue(withNull.locationIds().isEmpty());

        List<Long> mutable = new ArrayList<>(List.of(1L, 2L));
        JwtPrincipal withList = new JwtPrincipal(2L, "v@example.com", Role.MANAGER, 11L, mutable);
        mutable.add(3L);

        assertEquals(List.of(1L, 2L), withList.locationIds());
        assertThrows(UnsupportedOperationException.class, () -> withList.locationIds().add(9L));
    }

    @Test
    void passwordConfigProvidesBcryptEncoder() {
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
        assertTrue(encoder.matches("secret", encoder.encode("secret")));
    }

    @Test
    void swaggerConfigBuildsOpenApiWithBearerScheme() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openApi = config.openAPI();

        assertEquals("IK-Control API", openApi.getInfo().getTitle());
        assertEquals("1.0.0", openApi.getInfo().getVersion());
        assertEquals("Bearer Auth", openApi.getSecurity().get(0).keySet().iterator().next());
        assertEquals("bearer", openApi.getComponents()
                .getSecuritySchemes()
                .get("Bearer Auth")
                .getScheme());
    }
}
