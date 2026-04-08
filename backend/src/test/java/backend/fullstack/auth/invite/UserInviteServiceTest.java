package backend.fullstack.auth.invite;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import backend.fullstack.organization.Organization;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class UserInviteServiceTest {

    @Mock
    private UserInviteTokenRepository userInviteTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;

    private UserInviteService userInviteService;

    @BeforeEach
    void setUp() {
        InviteProperties inviteProperties = new InviteProperties();
        inviteProperties.setFrontendBaseUrl("http://localhost:5173");
        inviteProperties.setFromAddress("no-reply@test.local");

        userInviteService = new UserInviteService(
                userInviteTokenRepository,
                userRepository,
                passwordEncoder,
                mailSender,
                inviteProperties
        );
    }

    @Test
    void acceptInviteRejectsInvalidToken() {
        when(userInviteTokenRepository.findByTokenHash(hash("invalid"))).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> userInviteService.acceptInvite("invalid", "Password1"));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptInviteRejectsExpiredToken() {
        UserInviteToken token = UserInviteToken.builder()
                .id(1L)
                .userId(7L)
                .tokenHash(hash("expired-token"))
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .consumedAt(null)
                .build();

        when(userInviteTokenRepository.findByTokenHash(hash("expired-token"))).thenReturn(Optional.of(token));

        assertThrows(AccessDeniedException.class, () -> userInviteService.acceptInvite("expired-token", "Password1"));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptInviteRejectsReusedToken() {
        UserInviteToken token = UserInviteToken.builder()
                .id(1L)
                .userId(7L)
                .tokenHash(hash("used-token"))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .consumedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        when(userInviteTokenRepository.findByTokenHash(hash("used-token"))).thenReturn(Optional.of(token));

        assertThrows(AccessDeniedException.class, () -> userInviteService.acceptInvite("used-token", "Password1"));
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptInviteConsumesTokenAndActivatesUser() {
        UserInviteToken token = UserInviteToken.builder()
                .id(1L)
                .userId(7L)
                .tokenHash(hash("valid-token"))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .consumedAt(null)
                .build();

        User user = User.builder()
                .id(7L)
                .email("user@everest.no")
                .firstName("User")
                .lastName("Test")
                .passwordHash("old")
                .role(Role.STAFF)
                .organization(Organization.builder().id(1L).name("Everest").organizationNumber("123456789").build())
                .isActive(false)
                .build();

        when(userInviteTokenRepository.findByTokenHash(hash("valid-token"))).thenReturn(Optional.of(token));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");

        userInviteService.acceptInvite("valid-token", "Password1");

        verify(userRepository).save(user);
        verify(userInviteTokenRepository).save(token);
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
