package com.example.aiimage.controller;

import com.example.aiimage.dto.ImageGenerateResponse;
import com.example.aiimage.security.AuthenticatedUser;
import com.example.aiimage.service.ComfyService;
import com.example.aiimage.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.aiimage.service.ImageService.ImageDownload;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import com.example.aiimage.dto.ImageGenerateRequest;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;
    private final ComfyService comfyService;

    public ImageController(
            ImageService imageService,
            ComfyService comfyService
    ) {
        this.imageService = imageService;
        this.comfyService = comfyService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ImageGenerateResponse> generateImage(
            @Valid @RequestBody ImageGenerateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        ImageGenerateResponse result =
                imageService.generateAndSave(
                        user.userId(),
                        request.prompt().trim(),
                        request.negativePrompt(),
                        request.width(),
                        request.height()
                );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/view")
    public ResponseEntity<byte[]> viewImage(
            @RequestParam String filename,
            @RequestParam(defaultValue = "") String subfolder,
            @RequestParam(defaultValue = "output") String type
    ) {
        byte[] imageBytes = comfyService.getImage(
                filename,
                subfolder,
                type
        );

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyImages(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "false") boolean favorite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        int safePage = Math.max(page, 0);

        int safeSize = Math.min(
                Math.max(size, 1),
                50
        );

        return ResponseEntity.ok(
                imageService.getMyImages(
                        user.userId(),
                        q,
                        favorite,
                        safePage,
                        safeSize
                )
        );
    }

    @GetMapping("/{imageId}/download")
    public ResponseEntity<byte[]> downloadImage(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        ImageDownload download =
                imageService.downloadMyImage(
                        imageId,
                        user.userId()
                );

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(
                                download.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .contentLength(download.data().length)
                .body(download.data());
    }

    @GetMapping("/public/{imageId}/download")
    public ResponseEntity<byte[]> downloadPublicImage(
            @PathVariable String imageId
    ) {
        try {
            ImageDownload download =
                    imageService.downloadPublicImage(
                            imageId
                    );

            ContentDisposition disposition =
                    ContentDisposition.attachment()
                            .filename(
                                    download.filename(),
                                    StandardCharsets.UTF_8
                            )
                            .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposition.toString()
                    )
                    .contentLength(download.data().length)
                    .body(download.data());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new byte[0]);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(new byte[0]);
        }
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<?> getImageDetail(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        return ResponseEntity.ok(
                imageService.getMyImage(
                        imageId,
                        user.userId()
                )
        );
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(
                Math.max(size, 1),
                50
        );

        String safeSort =
                "popular".equalsIgnoreCase(sort)
                        ? "popular"
                        : "latest";

        return ResponseEntity.ok(
                imageService.getPublicImages(
                        safePage,
                        safeSize,
                        safeSort
                )
        );
    }

    @GetMapping("/public/{imageId}/view")
    public ResponseEntity<byte[]> viewPublicImage(
            @PathVariable String imageId
    ) {
        try {
            byte[] imageBytes =
                    imageService.getPublicImage(imageId);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public/{imageId}")
    public ResponseEntity<?> getPublicImageDetail(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        try {
            String currentUserId = null;

            if (
                    authentication != null &&
                            authentication.getPrincipal()
                                    instanceof AuthenticatedUser user
            ) {
                currentUserId = user.userId();
            }

            return ResponseEntity.ok(
                    imageService.getPublicImageDetail(
                            imageId,
                            currentUserId
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        imageService.deleteMyImage(
                imageId,
                user.userId()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "이미지가 삭제되었습니다."
                )
        );
    }

    @PatchMapping("/{imageId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        boolean favorite =
                imageService.toggleFavorite(
                        imageId,
                        user.userId()
                );

        return ResponseEntity.ok(
                Map.of(
                        "favorite",
                        favorite
                )
        );
    }

    @PatchMapping("/{imageId}/visibility")
    public ResponseEntity<?> toggleVisibility(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        boolean publicImage =
                imageService.toggleVisibility(
                        imageId,
                        user.userId()
                );

        return ResponseEntity.ok(
                Map.of(
                        "publicImage",
                        publicImage
                )
        );
    }

    @PostMapping("/public/{imageId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable String imageId,
            Authentication authentication
    ) {
        AuthenticatedUser user =
                (AuthenticatedUser)
                        authentication.getPrincipal();

        int likeCount =
                imageService.toggleLike(
                        imageId,
                        user.userId()
                );

        return ResponseEntity.ok(
                Map.of(
                        "likeCount",
                        likeCount
                )
        );
    }
}