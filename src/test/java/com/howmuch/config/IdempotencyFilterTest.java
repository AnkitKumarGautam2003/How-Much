package com.howmuch.config;

import com.howmuch.service.IdempotencyService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyFilterTest {

    @Test
    void generatesDifferentRequestHashesForDifferentBodies() throws Exception {
        IdempotencyService idempotencyService = mock(IdempotencyService.class);
        when(idempotencyService.findByKey(anyString())).thenReturn(Optional.empty());
        IdempotencyFilter filter = new IdempotencyFilter(idempotencyService);

        runRequest(filter, "key-1", "{\"amount\":100}");
        runRequest(filter, "key-2", "{\"amount\":200}");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(idempotencyService, times(2)).save(anyString(), hashCaptor.capture(), org.mockito.ArgumentMatchers.anyInt(), anyString());

        assertNotEquals(hashCaptor.getAllValues().get(0), hashCaptor.getAllValues().get(1));
    }

    private static void runRequest(IdempotencyFilter filter, String key, String body) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/loans");
        request.addHeader("Idempotency-Key", key);
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            String requestBody = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(body, requestBody);
            res.setContentType("application/json");
            res.getWriter().write("{\"status\":\"ok\"}");
        };

        filter.doFilter(request, response, chain);
    }
}
