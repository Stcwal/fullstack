package backend.fullstack.auth.invite;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

/**
 * Handles one-time user invites and invite acceptance.
 */
@Service
public class UserInviteService {

    private static final Logger logger = LoggerFactory.getLogger(UserInviteService.class);
    private static final int TOKEN_BYTES = 48;

    private final UserInviteTokenRepository userInviteTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Optional<JavaMailSender> mailSender;
    private final InviteProperties inviteProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserInviteService(
            UserInviteTokenRepository userInviteTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Optional<JavaMailSender> mailSender,
            InviteProperties inviteProperties
    ) {
        this.userInviteTokenRepository = userInviteTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.inviteProperties = inviteProperties;
    }

    @Transactional
    public void createAndSendInvite(User user) {
        userInviteTokenRepository.deleteByUserIdAndConsumedAtIsNull(user.getId());

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        UserInviteToken inviteToken = UserInviteToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        userInviteTokenRepository.save(inviteToken);
        sendInviteEmail(user, rawToken);
    }

    @Transactional
    public void acceptInvite(String token, String password) {
        String tokenHash = hashToken(token);
        UserInviteToken inviteToken = userInviteTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AccessDeniedException("Invalid invite token"));

        if (inviteToken.getConsumedAt() != null) {
            throw new AccessDeniedException("Invite token has already been used");
        }

        if (inviteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException("Invite token has expired");
        }

        User user = userRepository.findById(inviteToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invited user not found"));

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);
        userRepository.save(user);

        inviteToken.setConsumedAt(LocalDateTime.now());
        userInviteTokenRepository.save(inviteToken);
    }

    private void sendInviteEmail(User user, String token) {
        String base = trimTrailingSlash(inviteProperties.getFrontendBaseUrl());
        String inviteLink = base + "/set-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(inviteProperties.getFromAddress());
        message.setSubject("Set up your account");
        message.setText("Hi " + user.getFirstName() + ",\n\n"
                + "Your account has been created. Use this link to set your password:\n"
                + inviteLink + "\n\n"
                + "This link expires in 24 hours and can only be used once.\n");

        if (mailSender.isEmpty()) {
            logger.warn("Mail sender not configured — invite email NOT sent to userId={} email={}", user.getId(), user.getEmail());
            return;
        }
        mailSender.get().send(message);
        logger.info("Invite email sent to userId={} email={}", user.getId(), user.getEmail());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:5173";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
