package com.example.aiimage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ImageGenerateRequest(

        @NotBlank(message = "프롬프트를 입력해 주세요.")
        String prompt,

        String negativePrompt,

        @Min(value = 512, message = "가로 크기는 512 이상이어야 합니다.")
        @Max(value = 1536, message = "가로 크기는 1536 이하여야 합니다.")
        int width,

        @Min(value = 512, message = "세로 크기는 512 이상이어야 합니다.")
        @Max(value = 1536, message = "세로 크기는 1536 이하여야 합니다.")
        int height

) {
}