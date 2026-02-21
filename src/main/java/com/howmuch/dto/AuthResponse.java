package com.howmuch.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
