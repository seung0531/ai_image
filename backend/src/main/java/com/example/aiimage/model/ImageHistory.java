package com.example.aiimage.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Document(collection = "images")
public class ImageHistory {

    @Id
    private String id;

    private String userId;

    private String prompt;

    private String imageUrl;

    private LocalDateTime createdAt;

    private String filename;

    private String subfolder;

    private String type;

    private String negativePrompt;

    private Integer  width;

    private Integer  height;

    private boolean favorite;

    private boolean publicImage;

    private List<String> likedUserIds = new ArrayList<>();

    private int likeCount;

    public ImageHistory() {
    }

    public ImageHistory(
            String userId,
            String prompt,
            String negativePrompt,
            Integer width,
            Integer height,
            String imageUrl,
            String filename,
            String subfolder,
            String type,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.prompt = prompt;
        this.negativePrompt = negativePrompt;
        this.width = width;
        this.height = height;
        this.imageUrl = imageUrl;
        this.filename = filename;
        this.subfolder = subfolder;
        this.type = type;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFilename() {
        return filename;
    }

    public String getSubfolder() {
        return subfolder;
    }

    public String getType() {
        return type;
    }

    public String getNegativePrompt() {
        return negativePrompt;
    }

    public Integer  getWidth() {
        return width;
    }

    public Integer  getHeight() {
        return height;
    }

    public boolean isFavorite() { return favorite; }

    public boolean isPublicImage() { return publicImage; }

    public List<String> getLikedUserIds() { return likedUserIds; }

    public int getLikeCount() { return likeCount; }


    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNegativePrompt(String negativePrompt) {
        this.negativePrompt = negativePrompt;
    }

    public void setWidth(Integer  width) {
        this.width = width;
    }

    public void setHeight(Integer  height) {
        this.height = height;
    }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public void setPublicImage(boolean publicImage) { this.publicImage = publicImage; }

    public void setLikedUserIds(List<String> likedUserIds) { this.likedUserIds = likedUserIds; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}