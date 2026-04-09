package backend.fullstack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

class SecurityConfigBeansTest {

    @Test
    void userDetailsServiceReturnsUserOrThrows() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = mock(User.class);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        SecurityConfig config = new SecurityConfig(
                new JwtAuthFilter(new JwtUtil(new JwtProperties())),
                new SecurityErrorHandler(new com.fasterxml.jackson.databind.ObjectMapper()),
                userRepository,
                new BCryptPasswordEncoder()
        );

        UserDetailsService service = config.userDetailsService();
        assertEquals(user, service.loadUserByUsername("user@example.com"));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@example.com"));
    }

    @Test
    void authenticationBeansAndCorsSourceAreConfigured() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        SecurityConfig config = new SecurityConfig(
                new JwtAuthFilter(new JwtUtil(new JwtProperties())),
                new SecurityErrorHandler(new com.fasterxml.jackson.databind.ObjectMapper()),
                userRepository,
                new BCryptPasswordEncoder()
        );

        DaoAuthenticationProvider provider = config.authenticationProvider();
        assertNotNull(provider);

        AuthenticationManager manager = mock(AuthenticationManager.class);
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(manager);
        assertEquals(manager, config.authenticationManager(authenticationConfiguration));

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(cors);
        assertNotNull(cors.getAllowedOrigins());
        assertNotNull(cors.getAllowedMethods());
        assertEquals(true, cors.getAllowCredentials());
    }

        @Test
        void privateBootstrapAuthorizationDecisionReflectsUserCount() throws Exception {
        UserRepository emptyRepo = mock(UserRepository.class);
        when(emptyRepo.count()).thenReturn(0L);
        SecurityConfig allowConfig = new SecurityConfig(
            new JwtAuthFilter(new JwtUtil(new JwtProperties())),
            new SecurityErrorHandler(new com.fasterxml.jackson.databind.ObjectMapper()),
            emptyRepo,
            new BCryptPasswordEncoder()
        );

        Method method = SecurityConfig.class.getDeclaredMethod(
            "canAccessBootstrapSetup",
            Supplier.class,
            RequestAuthorizationContext.class
        );
        method.setAccessible(true);

        AuthorizationDecision allowDecision = (AuthorizationDecision) method.invoke(
            allowConfig,
            (Supplier<Authentication>) () -> null,
            new RequestAuthorizationContext(new MockHttpServletRequest())
        );
        assertEquals(true, allowDecision.isGranted());

        UserRepository nonEmptyRepo = mock(UserRepository.class);
        when(nonEmptyRepo.count()).thenReturn(2L);
        SecurityConfig denyConfig = new SecurityConfig(
            new JwtAuthFilter(new JwtUtil(new JwtProperties())),
            new SecurityErrorHandler(new com.fasterxml.jackson.databind.ObjectMapper()),
            nonEmptyRepo,
            new BCryptPasswordEncoder()
        );

        AuthorizationDecision denyDecision = (AuthorizationDecision) method.invoke(
            denyConfig,
            (Supplier<Authentication>) () -> null,
            new RequestAuthorizationContext(new MockHttpServletRequest())
        );
        assertEquals(false, denyDecision.isGranted());
        }
}
