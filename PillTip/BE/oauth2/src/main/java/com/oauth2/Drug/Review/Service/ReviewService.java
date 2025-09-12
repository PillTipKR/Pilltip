package com.oauth2.Drug.Review.Service;

import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.Review.Domain.*;
import com.oauth2.Drug.Review.Dto.*;
import com.oauth2.Drug.Review.Repository.*;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final DrugRepository drugRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final TagRepository tagRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    /**
     * 리뷰 생성
     */
    public Long createReview(User user, ReviewCreateRequest request, List<MultipartFile> images) {
        Drug drug = drugRepository.findById(request.drugId())
                .orElseThrow(() -> new RuntimeException("약을 찾을 수 없습니다"));

        Review review = new Review();
        review.setUser(user);
        review.setDrug(drug);
        review.setRating(request.rating());
        review.setContent(request.content());

        reviewRepository.save(review);

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            String baseDir = System.getProperty("user.dir") + "/upload/review/";
            File dir = new File(baseDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException("이미지 저장 폴더 생성 실패: " + baseDir);
                }
            }


            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File saveFile = new File(dir, fileName);
                System.out.println("실제 저장 경로: " + saveFile.getAbsolutePath());
                try {
                    file.transferTo(saveFile);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
                }

                // 저장된 이미지 URL 생성
                String imageUrl = "/review/" + fileName; // 접근 URL 기준 (ex. http://localhost:8080/review/abc.jpg)

                ReviewImage image = new ReviewImage();
                image.setReview(review);
                image.setImageUrl(imageUrl);
                image.setSortOrder(i);
                reviewImageRepository.save(image);
            }
        }

        // 태그 저장
        request.tags().forEach((typeStr, tagNames) -> {
            TagType type = TagType.from(typeStr);
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByNameAndType(tagName, type)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName, type)));

                ReviewTag reviewTag = new ReviewTag();
                reviewTag.setReview(review);
                reviewTag.setTag(tag);
                reviewTagRepository.save(reviewTag);
            }
        });

        return review.getId();
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다"));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<UserReviewResponse> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();

        // 좋아요 여부 최적화 조회
        List<Long> likedIds = reviewLikeRepository.findLikedReviewIds(userId, reviewIds);
        Set<Long> likedIdSet = new HashSet<>(likedIds);

        return reviews.stream()
                .map(r -> new UserReviewResponse(
                        r.getDrug().getId(),
                        r.getDrug().getName(),
                        ReviewResponse.from(r, userId, likedIdSet.contains(r.getId()))
                ))
                .toList();
    }



    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPagedReviews(Long drugId, Long userId, Pageable pageable) {
        // 1. 기본 리뷰 페이징 + 정렬
        Page<Review> page = reviewRepository.findByDrugId(drugId, pageable);
        List<Review> reviews = page.getContent();

        // 2. 좋아요 여부 최적화 조회
        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        List<Long> likedIds = reviewLikeRepository.findLikedReviewIds(userId, reviewIds);
        Set<Long> likedIdSet = new HashSet<>(likedIds);

        // 3. DTO 변환 + isLiked 적용
        List<ReviewResponse> responseList = reviews.stream()
                .map(r -> ReviewResponse.from(r, userId, likedIdSet.contains(r.getId())))
                .toList();

        return new PageImpl<>(responseList, pageable, page.getTotalElements());
    }




    public Sort getSort(String key, String direction) {
        Sort.Direction dir;
        try {
            dir = Sort.Direction.fromString(direction); // "asc" 또는 "desc" 처리
        } catch (IllegalArgumentException e) {
            dir = Sort.Direction.DESC; // 기본값
        }
        System.out.println("Received sortKey = " + key + ", direction = " + direction);
        return switch (key.toLowerCase()) {
            case "rating" -> Sort.by(dir, "rating");
            case "likes" -> Sort.by(dir, "likeCount");
            case "latest", "createdat" -> Sort.by(dir, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬
        };
    }



    @Transactional(readOnly = true)
    public ReviewStats getReviewWithRating(Long drugId) {
        List<Review> reviews = reviewRepository.findByDrugIdWithReviewTags(drugId); // 단일 호출

        RatingStatsResponse ratingStats = computeRatingStatsFromReviews(reviews);
        Map<TagType, TagStatsDto> tagStats = computeTagStatsFromReviews(reviews);
        Long like = ratingStats.getRatingCounts().entrySet().stream()
                .filter(entry -> entry.getKey() >= 4)
                .mapToLong(Map.Entry::getValue)
                .sum();
        return new ReviewStats(
                (long) reviews.size(),
                like,
                ratingStats,
                tagStats
        );
    }

    public RatingStatsResponse computeRatingStatsFromReviews(List<Review> reviews) {
        double avg = reviews.stream()
                .mapToDouble(Review::getRating)
                .average().orElse(0.0);

        Map<Integer, Long> buckets = IntStream.rangeClosed(1, 5)
                .boxed().collect(Collectors.toMap(i -> i, i -> 0L));

        for (Review r : reviews) {
            int b = (int) Math.floor(r.getRating());
            buckets.put(b, buckets.get(b) + 1);
        }

        return new RatingStatsResponse(avg, buckets);
    }

    public Map<TagType, TagStatsDto> computeTagStatsFromReviews(List<Review> reviews) {
        Map<TagType, Map<String, Long>> grouped = new EnumMap<>(TagType.class);

        for (Review r : reviews) {
            for (ReviewTag rt : r.getReviewTags()) {
                Tag tag = rt.getTag();
                grouped
                        .computeIfAbsent(tag.getType(), k -> new HashMap<>())
                        .merge(tag.getName(), 1L, Long::sum);
            }
        }

        Map<TagType, TagStatsDto> result = new EnumMap<>(TagType.class);

        for (TagType type : TagType.values()) {
            Map<String, Long> tagMap = grouped.getOrDefault(type, Map.of());
            long total = 0;
            long max = 0;
            String mostUsed = null;

            for (var entry : tagMap.entrySet()) {
                total += entry.getValue();
                if (entry.getValue() > max) {
                    mostUsed = entry.getKey();
                    max = entry.getValue();
                }
            }

            result.put(type, new TagStatsDto(
                    mostUsed != null? mostUsed:"",
                    max,
                    total
            ));
        }

        return result;
    }


}

