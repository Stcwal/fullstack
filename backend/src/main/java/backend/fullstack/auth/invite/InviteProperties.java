package backend.fullstack.auth.invite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized settings used for invite email generation.
 */
@Configuration
@ConfigurationProperties(prefix = "app.invite")
public class InviteProperties {

    private String frontendBaseUrl = "http://localhost:5173";
    private String fromAddress = "no-reply@iksystem.local";

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
