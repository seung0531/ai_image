package com.example.aiimage.repository;

import com.example.aiimage.model.ImageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImageRepository
        extends MongoRepository<ImageHistory, String> {

    Page<ImageHistory> findByUserId(
            String userId,
            Pageable pageable
    );

    Page<ImageHistory> findByUserIdAndPromptContainingIgnoreCase(
            String userId,
            String prompt,
            Pageable pageable
    );

    Page<ImageHistory> findByUserIdAndFavoriteTrue(
            String userId,
            Pageable pageable
    );

    Page<ImageHistory>
    findByUserIdAndFavoriteTrueAndPromptContainingIgnoreCase(
            String userId,
            String prompt,
            Pageable pageable
    );

    Optional<ImageHistory> findByIdAndUserId(
            String id,
            String userId
    );

    Page<ImageHistory> findByPublicImageTrue(
            Pageable pageable
    );

    Optional<ImageHistory> findByIdAndPublicImageTrue(
            String id
    );
}