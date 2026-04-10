package backend.fullstack.auth.invite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Sends emails on a background thread so SMTP hangs never block HTTP responses.
 */
@Component
public class AsyncMailSender {

    private static final Logger logger = LoggerFactory.getLogger(AsyncMailSender.class);

    private final Optional<JavaMailSender> mailSender;

    public AsyncMailSender(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void send(SimpleMailMessage message) {
        if (mailSender.isEmpty()) {
            return;
        }
        try {
            mailSender.get().send(message);
        } catch (Exception ex) {
            logger.error("Failed to send email to {}: {}", message.getTo(), ex.getMessage());
        }
    }
}
