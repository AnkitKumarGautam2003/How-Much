package com.howmuch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.howmuch.domain.IdempotencyRecord;
import com.howmuch.service.IdempotencyService;
import com.howmuch.util.HashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

        byte[] requestBody = request.getInputStream().readAllBytes();
        String requestBodyContent = new String(requestBody, StandardCharsets.UTF_8);
        String hash = HashUtil.sha256(request.getMethod() + ":" + request.getRequestURI() + ":" + requestBodyContent);

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

        HttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, requestBody);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(wrappedRequest, responseWrapper);

        byte[] bodyBytes = responseWrapper.getContentAsByteArray();
        String body = bodyBytes.length > 0
                ? new String(bodyBytes, StandardCharsets.UTF_8)
                : new ObjectMapper().writeValueAsString(java.util.Map.of("status", "ok"));

        if (responseWrapper.getStatus() < 500) {
            idempotencyService.save(key, hash, responseWrapper.getStatus(), body);
        }

        responseWrapper.copyBodyToResponse();
    }

    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        CachedBodyHttpServletRequest(HttpServletRequest request, byte[] cachedBody) {
            super(request);
            this.cachedBody = cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // no-op
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
