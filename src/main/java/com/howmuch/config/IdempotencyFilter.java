package com.howmuch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howmuch.domain.IdempotencyRecord;
import com.howmuch.service.IdempotencyService;
import com.howmuch.util.HashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private final IdempotencyService idempotencyService;

    public IdempotencyFilter(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod()) || request.getRequestURI().startsWith("/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key == null || key.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        requestWrapper.getParameterMap();
        String hash = HashUtil.sha256(request.getMethod() + ":" + request.getRequestURI());

        Optional<IdempotencyRecord> existing = idempotencyService.findByKey(key);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(hash)) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Idempotency key reused for a different request");
                return;
            }
            response.setStatus(record.getResponseStatus());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(record.getResponseBody());
            return;
        }

        filterChain.doFilter(requestWrapper, responseWrapper);

        byte[] bodyBytes = responseWrapper.getContentAsByteArray();
        String body = bodyBytes.length > 0
                ? new String(bodyBytes, StandardCharsets.UTF_8)
                : new ObjectMapper().writeValueAsString(java.util.Map.of("status", "ok"));

        if (responseWrapper.getStatus() < 500) {
            idempotencyService.save(key, hash, responseWrapper.getStatus(), body);
        }

        responseWrapper.copyBodyToResponse();
    }
}
