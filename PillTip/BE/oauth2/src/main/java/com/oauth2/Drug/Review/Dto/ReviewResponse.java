package com.oauth2.Drug.Review.Dto;

import com.oauth2.Drug.Review.Domain.Review;
import com.oauth2.Drug.Review.Domain.TagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {

    private Long id;
    private String userNickname;
    private String gender;
    private Boolean isMine;
    private Boolean isLiked;
    private Float rating;
    private int likeCount;
    private String content;
    private List<String> imageUrls;
    private List<String> efficacyTags;
    private List<String> sideEffectTags;
    private List<String> otherTags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static ReviewResponse from(Review review, Long userId, boolean isLiked) {
        String gender = review.getUser().getUserProfile().getGender().name().equals("MALE") ? "남성":"여성";
        return ReviewResponse.builder()
                .id(review.getId())
                .userNickname(review.getUser().getNickname())
                .gender(gender)
                .rating(review.getRating())
                .likeCount(review.getLikeCount())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .imageUrls(
                        review.getImages()
                                .stream()
                                .map(image -> {
                                    try {
                                        return encodeImageToBase64(image.getImageUrl());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .toList()
                )
                .efficacyTags(getTagsByType(review, TagType.EFFICACY))
                .sideEffectTags(getTagsByType(review, TagType.SIDE_EFFECT))
                .otherTags(getTagsByType(review, TagType.OTHER))
                .isMine(review.getUser().getId().equals(userId))
                .isLiked(isLiked)
                .build();
    }

    private static String encodeImageToBase64(String imagePath) throws IOException {
        String w = "upload";
        Path path = Paths.get(w+imagePath);
        String mimeType = Files.probeContentType(path); // 예: image/jpeg
        byte[] bytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mimeType + ";base64," + base64;
    }

    private static List<String> getTagsByType(Review review, TagType type) {
        if (review.getReviewTags() == null) return List.of();

        return review.getReviewTags().stream()
                .filter(rt -> rt.getTag().getType() == type)
                .map(rt -> rt.getTag().getName())
                .toList();
    }

}

