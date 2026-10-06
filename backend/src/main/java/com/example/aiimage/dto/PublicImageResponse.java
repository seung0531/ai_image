package com.example.aiimage.dto;

import java.time.LocalDateTime;

public record PublicImageResponse(
        String id,
        String prompt,
        String negativePrompt,
        int width,
        int height,
        String imageUrl,
        LocalDateTime createdAt,
        int likeCount,
        boolean liked
) {
}