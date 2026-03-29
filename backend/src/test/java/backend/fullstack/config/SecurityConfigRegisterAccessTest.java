package backend.fullstack.config;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import backend.fullstack.user.UserRepository;

class SecurityConfigRegisterAccessTest {

    @Test
    void allowsAnonymousRegisterWhenNoUsersExist() {
        SecurityConfig config = new SecurityConfig(
                new JwtAuthFilter(new JwtUtil(new JwtProperties())),
                repositoryWithCount(0L),
                new BCryptPasswordEncoder()
        );

        boolean allowed = config.isRegisterAccessAllowed(null);

        assertTrue(allowed);
    }

    @Test
    void deniesAnonymousRegisterAfterBootstrap() {
        SecurityConfig config = new SecurityConfig(
                new JwtAuthFilter(new JwtUtil(new JwtProperties())),
                repositoryWithCount(3L),
                new BCryptPasswordEncoder()
        );

        boolean allowed = config.isRegisterAccessAllowed(null);

        assertFalse(allowed);
    }

    @Test
    void allowsAdminRegisterAfterBootstrap() {
        SecurityConfig config = new SecurityConfig(
                new JwtAuthFilter(new JwtUtil(new JwtProperties())),
                repositoryWithCount(3L),
                new BCryptPasswordEncoder()
        );

        UsernamePasswordAuthenticationToken adminAuth =
                new UsernamePasswordAuthenticationToken(
                        "admin@everest.no",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        boolean allowed = config.isRegisterAccessAllowed(adminAuth);

        assertTrue(allowed);
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
