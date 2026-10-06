package com.example.aiimage.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

    public static final String COOKIE_NAME = "access_token";

    private final long expiration;
    private final boolean secure;

    public AuthCookieService(
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.cookie.secure:false}") boolean secure
    ) {
        this.expiration = expiration;
        this.secure = secure;
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(expiration))
                .build();
    }

    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}