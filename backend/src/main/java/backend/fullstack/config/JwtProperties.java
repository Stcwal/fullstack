package backend.fullstack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized JWT configuration bound from application properties.
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "default-secret-key-must-be-at-least-256-bits-long-for-hs256!";
    private long expirationMs = 28800000;
    private String cookieName = "jwt";
    private boolean cookieSecure = true;

    /**
     * Accessor kept for compatibility with existing call sites.
     *
     * @return JWT signing secret
     */
    public String secret() { return secret; }

    /**
     * Accessor kept for compatibility with existing call sites.
     *
     * @return token validity in milliseconds
     */
    public long expirationMs() { return expirationMs; }

    /**
     * Accessor kept for compatibility with existing call sites.
     *
     * @return authentication cookie name
     */
    public String cookieName() { return cookieName; }

    /**
     * Accessor kept for compatibility with existing call sites.
     *
     * @return whether authentication cookie should be secure-only
     */
    public boolean cookieSecure() { return cookieSecure; }

    /**
     * @return JWT signing secret
     */
    public String getSecret() { return secret; }

    /**
     * @return token validity in milliseconds
     */
    public long getExpirationMs() { return expirationMs; }

    /**
     * @return authentication cookie name
     */
    public String getCookieName() { return cookieName; }

    /**
     * @return whether authentication cookie should be secure-only
     */
    public boolean isCookieSecure() { return cookieSecure; }

    /**
     * @param secret JWT signing secret
     */
    public void setSecret(String secret) { this.secret = secret; }

    /**
     * @param expirationMs token validity in milliseconds
     */
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

    /**
     * @param cookieName authentication cookie name
     */
    public void setCookieName(String cookieName) { this.cookieName = cookieName; }

    /**
     * @param cookieSecure whether authentication cookie should be secure-only
     */
    public void setCookieSecure(boolean cookieSecure) { this.cookieSecure = cookieSecure; }
}