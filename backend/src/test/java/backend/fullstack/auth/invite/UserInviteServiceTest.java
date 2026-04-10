package backend.fullstack.auth.invite;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
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
    @Mock
    private AsyncMailSender asyncMailSender;

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
                Optional.of(mailSender),
            asyncMailSender,
                inviteProperties
        );
    }

    @Test
    void createAndSendInviteReplacesExistingTokenAndSendsEmail() {
        User user = User.builder()
                .id(7L)
                .email("user@everest.no")
                .firstName("User")
                .lastName("Test")
                .role(Role.STAFF)
                .organization(Organization.builder().id(1L).name("Everest").organizationNumber("123456789").build())
                .build();

        userInviteService.createAndSendInvite(user);

        verify(userInviteTokenRepository).deleteByUserIdAndConsumedAtIsNull(7L);

        ArgumentCaptor<UserInviteToken> tokenCaptor = ArgumentCaptor.forClass(UserInviteToken.class);
        verify(userInviteTokenRepository).save(tokenCaptor.capture());

        UserInviteToken savedToken = tokenCaptor.getValue();
        assertEquals(7L, savedToken.getUserId());
        assertEquals(64, savedToken.getTokenHash().length());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(asyncMailSender).send(mailCaptor.capture());

        SimpleMailMessage sentMessage = mailCaptor.getValue();
        assertEquals("Set up your account", sentMessage.getSubject());
        assertEquals("no-reply@test.local", sentMessage.getFrom());
        assertEquals("user@everest.no", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getText().contains("http://localhost:5173/set-password?token="));
    }

    @Test
    void createAndSendInviteWithoutMailSenderStillCreatesToken() {
        InviteProperties inviteProperties = new InviteProperties();
        inviteProperties.setFrontendBaseUrl("http://localhost:5173");
        inviteProperties.setFromAddress("no-reply@test.local");

        UserInviteService serviceWithoutMail = new UserInviteService(
                userInviteTokenRepository,
                userRepository,
                passwordEncoder,
                Optional.empty(),
            asyncMailSender,
                inviteProperties
        );

        User user = User.builder()
                .id(8L)
                .email("nomail@everest.no")
                .firstName("No")
                .lastName("Mail")
                .role(Role.STAFF)
                .organization(Organization.builder().id(1L).name("Everest").organizationNumber("123456789").build())
                .build();

        serviceWithoutMail.createAndSendInvite(user);

        verify(userInviteTokenRepository).deleteByUserIdAndConsumedAtIsNull(8L);
        verify(userInviteTokenRepository, org.mockito.Mockito.times(1)).save(org.mockito.ArgumentMatchers.any(UserInviteToken.class));
        verify(asyncMailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
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
