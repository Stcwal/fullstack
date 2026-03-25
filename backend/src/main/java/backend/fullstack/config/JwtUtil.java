package backend.fullstack.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Utility for creating, parsing and validating JWT tokens and cookies.
 */
@Component
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    /**
     * Creates a JWT utility using externalized JWT properties.
     *
     * @param jwtProperties JWT configuration values
     */
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the JWT value from the configured authentication cookie.
     *
     * @param request incoming HTTP request
     * @return token value, or {@code null} when no cookie is present
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtProperties.getCookieName());
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * Generates a signed JWT for the authenticated user.
     *
     * @param email user email (JWT subject)
     * @param userId internal user id
     * @param role user role
     * @param organizationId organization id claim
     * @param locationIds location scope claim
     * @return compact JWT string
     */
    public String generateToken(
            String email,
            Long userId,
            String role,
            Long organizationId,
            List<Long> locationIds
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .claim("organizationId", organizationId)
                .claim("locationIds", locationIds)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generates an HttpOnly cookie containing a freshly signed JWT.
     *
     * @param email user email (JWT subject)
     * @param userId internal user id
     * @param role user role
     * @param organizationId organization id claim
     * @param locationIds location scope claim
     * @return configured JWT response cookie
     */
    public ResponseCookie generateJwtCookie(
            String email,
            Long userId,
            String role,
            Long organizationId,
            List<Long> locationIds
    ) {
        String jwt = generateToken(email, userId, role, organizationId, locationIds);

        return ResponseCookie.from(jwtProperties.getCookieName(), jwt)
                .path("/api")
                .maxAge(jwtProperties.getExpirationMs() / 1000)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .build();
    }

    /**
     * Generates an expired JWT cookie used for logout.
     *
     * @return cookie that clears the authentication cookie in the browser
     */
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtProperties.getCookieName(), "")
                .path("/api")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .build();
    }

    /**
     * Validates a token signature and expiration.
     *
     * @param authToken JWT token to validate
     * @return true when valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts all claims from a JWT.
     *
     * @param token JWT token
     * @return parsed JWT claims
     */
    public Claims getClaimsFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
