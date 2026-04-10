package backend.fullstack.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.fullstack.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Writes security-layer authentication and authorization failures using the
 * same JSON error envelope as controller exceptions.
 * 
 * @version 1.0
 * @since 04.04.26
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityErrorHandler(ObjectMapper objectMapper, CorsConfigurationSource corsConfigurationSource) {
        this.objectMapper = objectMapper;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        writeErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                authException.getMessage() == null ? "Authentication is required" : authException.getMessage(),
                request.getRequestURI()
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        writeErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                accessDeniedException.getMessage() == null ? "Access denied" : accessDeniedException.getMessage(),
                request.getRequestURI()
        );
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String errorCode,
            String message,
            String path
    ) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                null,
                path
        );

        // Ensure CORS headers are present even on security error responses
        String origin = request.getHeader("Origin");
        if (origin != null) {
            var corsConfig = corsConfigurationSource.getCorsConfiguration(request);
            if (corsConfig != null) {
                List<String> allowed = corsConfig.getAllowedOrigins();
                if (allowed != null && allowed.contains(origin)) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                }
            }
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
