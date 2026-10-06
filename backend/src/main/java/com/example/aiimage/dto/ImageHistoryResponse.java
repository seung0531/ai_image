package com.example.aiimage.dto;

import java.time.LocalDateTime;

public record ImageHistoryResponse(
        String id,
        String prompt,
        String negativePrompt,
        int width,
        int height,
        String imageUrl,
        LocalDateTime createdAt,
        boolean favorite,
        boolean publicImage,
        int likeCount
) {
}