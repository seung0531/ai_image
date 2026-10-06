package com.example.aiimage.service;

import com.example.aiimage.dto.ImageGenerateResponse;
import com.example.aiimage.model.ImageHistory;
import com.example.aiimage.repository.ImageRepository;
import com.example.aiimage.service.ComfyService.ImageResult;
import org.springframework.stereotype.Service;
import com.example.aiimage.dto.ImageHistoryResponse;
import org.springframework.beans.factory.annotation.Value;
import com.example.aiimage.dto.ImagePageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.aiimage.dto.PublicImageResponse;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
@Service
public class ImageService {

    private final ComfyService comfyService;
    private final ImageRepository imageRepository;
    private final Path comfyOutputDirectory;

    public ImageService(
            ComfyService comfyService,
            ImageRepository imageRepository,
            @Value("${comfy.output-dir}") String comfyOutputDirectory
    ) {
        this.comfyService = comfyService;
        this.imageRepository = imageRepository;
        this.comfyOutputDirectory =
                Path.of(comfyOutputDirectory)
                        .toAbsolutePath()
                        .normalize();
    }

    public ImageGenerateResponse generateAndSave(
            String userId,
            String prompt,
            String negativePrompt,
            int width,
            int height
    ) {
        // 1. ComfyUI에서 이미지 생성
        ImageResult result =
                comfyService.generateImage(
                        prompt,
                        negativePrompt,
                        width,
                        height
                );

        // 2. MongoDB에 저장할 객체 생성
        ImageHistory imageHistory = new ImageHistory(
                userId,
                prompt,
                negativePrompt == null
                        ? ""
                        : negativePrompt.trim(),
                width,
                height,
                result.imageUrl(),
                result.filename(),
                result.subfolder(),
                result.type(),
                LocalDateTime.now()
        );

        // 3. MongoDB images 컬렉션에 저장
        ImageHistory savedImage =
                imageRepository.save(imageHistory);

        // 4. 프론트에 반환할 응답 생성
        return new ImageGenerateResponse(
                savedImage.getId(),
                result.promptId(),
                savedImage.getPrompt(),
                savedImage.getImageUrl(),
                savedImage.getCreatedAt()
        );
    }

    public ImagePageResponse getMyImages(
            String userId,
            String keyword,
            boolean favoriteOnly,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ImageHistory> imagePage;

        boolean hasKeyword =
                keyword != null && !keyword.isBlank();

        if (favoriteOnly && hasKeyword) {

            imagePage =
                    imageRepository
                            .findByUserIdAndFavoriteTrueAndPromptContainingIgnoreCase(
                                    userId,
                                    keyword.trim(),
                                    pageable
                            );

        } else if (favoriteOnly) {

            imagePage =
                    imageRepository
                            .findByUserIdAndFavoriteTrue(
                                    userId,
                                    pageable
                            );

        } else if (hasKeyword) {

            imagePage =
                    imageRepository
                            .findByUserIdAndPromptContainingIgnoreCase(
                                    userId,
                                    keyword.trim(),
                                    pageable
                            );

        } else {

            imagePage =
                    imageRepository.findByUserId(
                            userId,
                            pageable
                    );
        }

        List<ImageHistoryResponse> responses =
                imagePage.getContent()
                        .stream()
                        .map(image -> new ImageHistoryResponse(
                                image.getId(),
                                image.getPrompt() == null ? "" : image.getPrompt(),
                                image.getNegativePrompt() == null ? "" : image.getNegativePrompt(),
                                image.getWidth() == null ? 0 : image.getWidth(),
                                image.getHeight() == null ? 0 : image.getHeight(),
                                image.getImageUrl() == null ? "" : image.getImageUrl(),
                                image.getCreatedAt(),
                                image.isFavorite(),
                                image.isPublicImage(),
                                image.getLikeCount()
                        ))
                        .toList();

        return new ImagePageResponse(
                responses,
                imagePage.getNumber(),
                imagePage.getSize(),
                imagePage.getTotalPages(),
                imagePage.getTotalElements(),
                imagePage.isFirst(),
                imagePage.isLast()
        );
    }

    public void deleteMyImage(
            String imageId,
            String userId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndUserId(imageId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이미지를 찾을 수 없습니다."
                        )
                );

        deleteComfyFile(image);

        imageRepository.delete(image);
    }

    private void deleteComfyFile(ImageHistory image) {
        String filename = image.getFilename();

        // 기존 기록에는 filename이 없을 수 있으므로
        // 이 경우 DB 기록만 삭제한다.
        if (filename == null || filename.isBlank()) {
            return;
        }

        String subfolder = image.getSubfolder();

        Path targetDirectory = comfyOutputDirectory;

        if (subfolder != null && !subfolder.isBlank()) {
            targetDirectory = targetDirectory.resolve(subfolder);
        }

        Path imagePath = targetDirectory
                .resolve(filename)
                .toAbsolutePath()
                .normalize();

        // ../ 등을 이용한 경로 조작 공격 방지
        if (!imagePath.startsWith(comfyOutputDirectory)) {
            throw new IllegalArgumentException(
                    "올바르지 않은 이미지 경로입니다."
            );
        }

        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "ComfyUI 이미지 파일 삭제에 실패했습니다.",
                    e
            );
        }
    }

    public record ImageDownload(
            byte[] data,
            String filename
    ) {
    }

    public ImageDownload downloadMyImage(
            String imageId,
            String userId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndUserId(imageId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이미지를 찾을 수 없습니다."
                        )
                );

        if (
                image.getFilename() == null ||
                        image.getFilename().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "다운로드할 이미지 파일 정보가 없습니다."
            );
        }

        byte[] imageBytes = comfyService.getImage(
                image.getFilename(),
                image.getSubfolder() == null
                        ? ""
                        : image.getSubfolder(),
                image.getType() == null
                        ? "output"
                        : image.getType()
        );

        return new ImageDownload(
                imageBytes,
                image.getFilename()
        );
    }

    public ImageDownload downloadPublicImage(
            String imageId
    ) {
        ImageHistory image =
                imageRepository
                        .findByIdAndPublicImageTrue(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "공개된 이미지를 찾을 수 없습니다."
                                )
                        );

        if (
                image.getFilename() == null ||
                        image.getFilename().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "다운로드할 이미지 파일 정보가 없습니다."
            );
        }

        byte[] imageBytes = comfyService.getImage(
                image.getFilename(),
                image.getSubfolder() == null
                        ? ""
                        : image.getSubfolder(),
                image.getType() == null
                        ? "output"
                        : image.getType()
        );

        return new ImageDownload(
                imageBytes,
                image.getFilename()
        );
    }

    public boolean toggleFavorite(
            String imageId,
            String userId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndUserId(imageId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이미지를 찾을 수 없습니다."
                        )
                );

        boolean nextFavorite = !image.isFavorite();

        image.setFavorite(nextFavorite);

        imageRepository.save(image);

        return nextFavorite;
    }

    // ImageService.java

    public ImageHistoryResponse getMyImage(
            String imageId,
            String userId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndUserId(imageId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이미지를 찾을 수 없습니다."
                        )
                );

        return new ImageHistoryResponse(
                image.getId(),
                image.getPrompt() == null ? "" : image.getPrompt(),
                image.getNegativePrompt() == null ? "" : image.getNegativePrompt(),
                image.getWidth() == null ? 0 : image.getWidth(),
                image.getHeight() == null ? 0 : image.getHeight(),
                image.getImageUrl() == null ? "" : image.getImageUrl(),
                image.getCreatedAt(),
                image.isFavorite(),
                image.isPublicImage(),
                image.getLikeCount()
        );
    }

    public boolean toggleVisibility(
            String imageId,
            String userId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndUserId(imageId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이미지를 찾을 수 없습니다."
                        )
                );

        boolean nextValue = !image.isPublicImage();

        image.setPublicImage(nextValue);

        imageRepository.save(image);

        return nextValue;
    }

    public ImagePageResponse getPublicImages(
            int page,
            int size,
            String sort
    ) {
        Sort sorting;

        if ("popular".equalsIgnoreCase(sort)) {
            sorting = Sort.by(
                    Sort.Direction.DESC,
                    "likeCount"
            ).and(
                    Sort.by(
                            Sort.Direction.DESC,
                            "createdAt"
                    )
            );
        } else {
            sorting = Sort.by(
                    Sort.Direction.DESC,
                    "createdAt"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                sorting
        );

        Page<ImageHistory> imagePage =
                imageRepository.findByPublicImageTrue(
                        pageable
                );

        List<ImageHistoryResponse> responses =
                imagePage.getContent()
                        .stream()
                        .map(image ->
                                new ImageHistoryResponse(
                                        image.getId(),
                                        image.getPrompt() == null
                                                ? ""
                                                : image.getPrompt(),
                                        image.getNegativePrompt() == null
                                                ? ""
                                                : image.getNegativePrompt(),
                                        image.getWidth() == null
                                                ? 0
                                                : image.getWidth(),
                                        image.getHeight() == null
                                                ? 0
                                                : image.getHeight(),
                                        image.getImageUrl() == null
                                                ? ""
                                                : image.getImageUrl(),
                                        image.getCreatedAt(),
                                        image.isFavorite(),
                                        image.isPublicImage(),
                                        image.getLikeCount()
                                )
                        )
                        .toList();

        return new ImagePageResponse(
                responses,
                imagePage.getNumber(),
                imagePage.getSize(),
                imagePage.getTotalPages(),
                imagePage.getTotalElements(),
                imagePage.isFirst(),
                imagePage.isLast()
        );
    }

    public byte[] getPublicImage(String imageId) {

        ImageHistory image = imageRepository
                .findByIdAndPublicImageTrue(imageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "공개 이미지를 찾을 수 없습니다."
                        )
                );

        return comfyService.getImage(
                image.getFilename(),
                image.getSubfolder() == null
                        ? ""
                        : image.getSubfolder(),
                image.getType() == null
                        ? "output"
                        : image.getType()
        );
    }

    public PublicImageResponse getPublicImageDetail(
            String imageId,
            String currentUserId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndPublicImageTrue(imageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "공개 이미지를 찾을 수 없습니다."
                        )
                );

        List<String> likedUserIds =
                image.getLikedUserIds();

        boolean liked =
                currentUserId != null &&
                        likedUserIds.contains(currentUserId);

        return new PublicImageResponse(
                image.getId(),
                image.getPrompt() == null
                        ? ""
                        : image.getPrompt(),
                image.getNegativePrompt() == null
                        ? ""
                        : image.getNegativePrompt(),
                image.getWidth() == null
                        ? 0
                        : image.getWidth(),
                image.getHeight() == null
                        ? 0
                        : image.getHeight(),
                "/api/image/public/"
                        + image.getId()
                        + "/view",
                image.getCreatedAt(),
                likedUserIds.size(),
                liked
        );
    }

    public int toggleLike(
            String imageId,
            String userId
    ) {
        ImageHistory image = imageRepository
                .findByIdAndPublicImageTrue(imageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "공개 이미지를 찾을 수 없습니다."
                        )
                );

        List<String> likedUserIds =
                image.getLikedUserIds();

        if (likedUserIds.contains(userId)) {
            likedUserIds.remove(userId);
        } else {
            likedUserIds.add(userId);
        }

        image.setLikeCount(likedUserIds.size());

        imageRepository.save(image);

        return image.getLikeCount();
    }
}