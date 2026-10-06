package com.example.aiimage.dto;

import java.util.List;

public record ImagePageResponse(
        List<ImageHistoryResponse> images,
        int currentPage,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last
) {
}