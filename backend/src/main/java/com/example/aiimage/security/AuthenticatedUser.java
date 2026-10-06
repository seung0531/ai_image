package com.example.aiimage.security;

public record AuthenticatedUser(
        String userId,
        String email
) {
}