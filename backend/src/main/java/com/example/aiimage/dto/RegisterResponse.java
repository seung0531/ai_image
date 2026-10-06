package com.example.aiimage.dto;

public record RegisterResponse(
        String message,
        String userId,
        String email
) {
}