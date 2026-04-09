package backend.fullstack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

class SecurityErrorHandlerTest {

    @Test
    void commenceWritesUnauthorizedJsonResponse() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        SecurityErrorHandler handler = new SecurityErrorHandler(mapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new BadCredentialsException("bad credentials"));

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        JsonNode body = mapper.readTree(response.getContentAsString());
        assertEquals("UNAUTHORIZED", body.get("errorCode").asText());
        assertEquals("bad credentials", body.get("message").asText());
        assertEquals("/api/private", body.get("path").asText());
    }

    @Test
    void handleWritesForbiddenJsonResponseAndFallbackMessage() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        SecurityErrorHandler handler = new SecurityErrorHandler(mapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException(null));

        assertEquals(403, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        JsonNode body = mapper.readTree(response.getContentAsString());
        assertEquals("ACCESS_DENIED", body.get("errorCode").asText());
        assertTrue(body.get("message").asText().contains("Access denied"));
        assertEquals("/api/admin", body.get("path").asText());
    }

    @Test
    void commenceUsesFallbackMessageWhenAuthMessageIsNull() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        SecurityErrorHandler handler = new SecurityErrorHandler(mapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/private");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new AuthenticationException(null) {
            private static final long serialVersionUID = 1L;
        });

        JsonNode body = mapper.readTree(response.getContentAsString());
        assertTrue(body.get("message").asText().contains("Authentication is required"));
    }

    @Test
    void handleUsesCustomMessageWhenProvided() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        SecurityErrorHandler handler = new SecurityErrorHandler(mapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("specific denied"));

        JsonNode body = mapper.readTree(response.getContentAsString());
        assertEquals("specific denied", body.get("message").asText());
    }
}
