package backend.fullstack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import backend.fullstack.user.role.Role;
import io.jsonwebtoken.Claims;

class JwtAuthFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesFromCookieTokenWhenValid() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.getJwtFromCookies(org.mockito.ArgumentMatchers.any())).thenReturn("cookie-jwt");
        when(jwtUtil.validateJwtToken("cookie-jwt")).thenReturn(true);
        when(jwtUtil.getClaimsFromJwtToken("cookie-jwt")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user@example.com");
        when(claims.get("role", String.class)).thenReturn("MANAGER");
        when(claims.get("userId")).thenReturn(10);
        when(claims.get("organizationId")).thenReturn(20);
        when(claims.get("locationIds")).thenReturn(List.of(1, 2));

        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("user@example.com", principal.email());
        assertEquals(Role.MANAGER, principal.role());
        assertEquals(List.of(1L, 2L), principal.locationIds());
    }

    @Test
    void authenticatesFromBearerHeaderWhenCookieMissing() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        Claims claims = mock(Claims.class);
        when(jwtUtil.getJwtFromCookies(org.mockito.ArgumentMatchers.any())).thenReturn(null);
        when(jwtUtil.validateJwtToken("header-jwt")).thenReturn(true);
        when(jwtUtil.getClaimsFromJwtToken("header-jwt")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin@example.com");
        when(claims.get("role", String.class)).thenReturn("ROLE_ADMIN");
        when(claims.get("userId")).thenReturn(1L);
        when(claims.get("organizationId")).thenReturn(99L);
        when(claims.get("locationIds")).thenReturn("not-a-list");

        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-jwt");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        JwtPrincipal principal = (JwtPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(Role.ADMIN, principal.role());
        assertEquals(List.of(), principal.locationIds());
    }

    @Test
    void leavesSecurityContextEmptyForInvalidTokenOrErrors() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.getJwtFromCookies(org.mockito.ArgumentMatchers.any())).thenReturn("bad");
        when(jwtUtil.validateJwtToken("bad")).thenReturn(false);

        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        when(jwtUtil.getJwtFromCookies(org.mockito.ArgumentMatchers.any())).thenThrow(new RuntimeException("boom"));
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
