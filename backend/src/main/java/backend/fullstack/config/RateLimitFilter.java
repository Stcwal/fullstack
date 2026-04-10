package backend.fullstack.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple in-memory rate limiter for authentication endpoints.
 * Limits each IP to a fixed number of requests per time window
 * to mitigate brute-force attacks on login and registration.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, RateBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        RateBucket bucket = buckets.compute(ip, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new RateBucket(Instant.now().plus(WINDOW), 1);
            }
            existing.increment();
            return existing;
        });

        if (bucket.getCount() > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\",\"data\":null}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/auth/login")
                && !path.startsWith("/api/auth/register")
                && !path.startsWith("/api/auth/invite/accept");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateBucket {
        private final Instant expiresAt;
        private int count;

        RateBucket(Instant expiresAt, int count) {
            this.expiresAt = expiresAt;
            this.count = count;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        void increment() {
            count++;
        }

        int getCount() {
            return count;
        }
    }
}
