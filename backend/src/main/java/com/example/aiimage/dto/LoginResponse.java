package com.example.aiimage.dto;

public record LoginResponse(
        String message,
        String userId,
        String email
) {
}