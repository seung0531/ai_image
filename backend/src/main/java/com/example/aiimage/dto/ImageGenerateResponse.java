package com.example.aiimage.dto;

import java.time.LocalDateTime;

public record ImageGenerateResponse(
        String id,
        String promptId,
        String prompt,
        String imageUrl,
        LocalDateTime createdAt
) {
}