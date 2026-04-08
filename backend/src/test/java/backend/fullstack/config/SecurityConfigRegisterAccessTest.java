package backend.fullstack.config;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import backend.fullstack.user.UserRepository;

class SecurityConfigRegisterAccessTest {

    @Test
    void allowsAnonymousRegisterWhenNoUsersExist() {
        SecurityConfig config = configWithUserCount(0L);

        boolean allowed = config.isRegisterAccessAllowed(null);

        assertTrue(allowed);
    }

    @Test
    void deniesAnonymousRegisterAfterBootstrap() {
        SecurityConfig config = configWithUserCount(3L);

        boolean allowed = config.isRegisterAccessAllowed(null);

        assertFalse(allowed);
    }

    @Test
    void deniesAdminRegisterAfterBootstrap() {
        SecurityConfig config = configWithUserCount(3L);

        boolean allowed = config.isRegisterAccessAllowed(null);

        assertFalse(allowed);
    }

    @Test
    void allowsOrganizationCreateWhenNoUsersExist() {
        SecurityConfig config = configWithUserCount(0L);

        boolean allowed = config.isOrganizationCreateAccessAllowed(null);

        assertTrue(allowed);
    }

    @Test
    void deniesOrganizationCreateAfterBootstrap() {
        SecurityConfig config = configWithUserCount(3L);

        boolean allowed = config.isOrganizationCreateAccessAllowed(null);

        assertFalse(allowed);
    }

    private SecurityConfig configWithUserCount(long count) {
        return new SecurityConfig(
                new JwtAuthFilter(new JwtUtil(new JwtProperties())),
                new SecurityErrorHandler(new com.fasterxml.jackson.databind.ObjectMapper()),
                repositoryWithCount(count),
                new BCryptPasswordEncoder()
        );
    }

    private UserRepository repositoryWithCount(long count) {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("count".equals(method.getName())) {
                        return count;
                    }
                    if ("toString".equals(method.getName())) {
                        return "UserRepositoryProxy(count=" + count + ")";
                    }
                    throw new UnsupportedOperationException("Method not supported in this test: " + method.getName());
                }
        );
    }
}
