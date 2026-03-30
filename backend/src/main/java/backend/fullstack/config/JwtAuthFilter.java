package backend.fullstack.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import backend.fullstack.user.role.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security filter that authenticates requests using JWT from cookie or header.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Validates JWT and populates the Spring Security context when token is valid.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain next filter in chain
     * @throws ServletException when filter handling fails
     * @throws IOException when I/O fails
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtil.validateJwtToken(jwt)) {
                Claims claims = jwtUtil.getClaimsFromJwtToken(jwt);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                Long userId = asLong(claims.get("userId"));
                Long organizationId = asLong(claims.get("organizationId"));
                List<Long> locationIds = asLongList(claims.get("locationIds"));

                if (role != null && !role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                if (email != null && role != null) {
                    Role parsedRole = Role.valueOf(role.replace("ROLE_", ""));
                    JwtPrincipal principal = new JwtPrincipal(userId, email, parsedRole, organizationId, locationIds);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                        );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolves a JWT from request cookie first, then from Authorization header.
     *
     * @param request incoming HTTP request
     * @return JWT token or {@code null} if not present
     */
    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtil.getJwtFromCookies(request);
        if (jwt != null) {
            return jwt;
        }

        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private List<Long> asLongList(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }

        return rawList.stream()
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::longValue)
                .toList();
    }
}