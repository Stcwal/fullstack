package backend.fullstack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("0123456789012345678901234567890123456789012345678901234567890123");
        properties.setExpirationMs(60_000L);
        properties.setCookieName("jwt");
        properties.setCookieSecure(false);
        jwtUtil = new JwtUtil(properties);
    }

    @Test
    void generateValidateAndParseToken() {
        String token = jwtUtil.generateToken("user@example.com", 10L, "MANAGER", 20L, List.of(1L, 2L));

        assertTrue(jwtUtil.validateJwtToken(token));

        Claims claims = jwtUtil.getClaimsFromJwtToken(token);
        assertEquals("user@example.com", claims.getSubject());
        assertEquals("MANAGER", claims.get("role", String.class));
        assertEquals(10L, ((Number) claims.get("userId")).longValue());
        assertEquals(20L, ((Number) claims.get("organizationId")).longValue());
        assertNotNull(claims.get("locationIds"));
    }

    @Test
    void getJwtFromCookiesReturnsCookieValueOrNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", "token-123"));
        assertEquals("token-123", jwtUtil.getJwtFromCookies(request));

        MockHttpServletRequest requestWithoutCookie = new MockHttpServletRequest();
        assertEquals(null, jwtUtil.getJwtFromCookies(requestWithoutCookie));
    }

    @Test
    void generateCookiesUseExpectedDefaults() {
        ResponseCookie jwtCookie = jwtUtil.generateJwtCookie("u@example.com", 1L, "ADMIN", 2L, List.of(7L));
        assertEquals("jwt", jwtCookie.getName());
        assertTrue(jwtCookie.toString().contains("HttpOnly"));
        assertTrue(jwtCookie.toString().contains("SameSite=Strict"));

        ResponseCookie fromTokenCookie = jwtUtil.generateJwtCookieFromToken("abc");
        assertEquals("jwt", fromTokenCookie.getName());
        assertEquals("abc", fromTokenCookie.getValue());

        ResponseCookie cleanCookie = jwtUtil.getCleanJwtCookie();
        assertEquals("", cleanCookie.getValue());
        assertTrue(cleanCookie.toString().contains("Max-Age=0"));
    }

    @Test
    void validateJwtTokenReturnsFalseForInvalidInput() {
        assertFalse(jwtUtil.validateJwtToken("not-a-jwt"));
        assertFalse(jwtUtil.validateJwtToken(""));
    }
}
