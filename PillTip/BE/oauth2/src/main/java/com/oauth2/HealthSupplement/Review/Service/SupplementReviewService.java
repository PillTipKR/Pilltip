package com.oauth2.HealthSupplement.Review.Service;

import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.Review.Domain.*;
import com.oauth2.Drug.Review.Dto.*;
import com.oauth2.HealthSupplement.Review.Dto.SupplementReviewResponse;
import com.oauth2.HealthSupplement.Review.Dto.UserSupplementReviewResponse;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReview;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReviewImage;
import com.oauth2.HealthSupplement.Review.Entity.SupplementReviewTag;
import com.oauth2.HealthSupplement.Review.Entity.SupplementTag;
import com.oauth2.HealthSupplement.Review.Repository.*;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class SupplementReviewService {

    private final SupplementReviewRepository supplementReviewRepository;
    private final HealthSupplementRepository healthSupplementRepository;
    private final SupplementReviewImageRepository supplementReviewImageRepository;
    private final SupplementTagRepository supplementTagRepository;
    private final SupplementReviewTagRepository supplementReviewTagRepository;
    private final SupplementReviewLikeRepository supplementReviewLikeRepository;

    /**
     * 리뷰 생성
     */
    public Long createReview(User user, ReviewCreateRequest request, List<MultipartFile> images) {
        HealthSupplement supplement = healthSupplementRepository.findById(request.drugId())
                .orElseThrow(() -> new RuntimeException("약을 찾을 수 없습니다"));

        SupplementReview review = new SupplementReview();
        review.setUser(user);
        review.setSupplement(supplement);
        review.setRating(request.rating());
        review.setContent(request.content());

        supplementReviewRepository.save(review);

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

                SupplementReviewImage image = new SupplementReviewImage();
                image.setReview(review);
                image.setImageUrl(imageUrl);
                image.setSortOrder(i);
                supplementReviewImageRepository.save(image);
            }
        }

        // 태그 저장
        request.tags().forEach((typeStr, tagNames) -> {
            TagType type = TagType.from(typeStr);
            for (String tagName : tagNames) {
                SupplementTag tag = supplementTagRepository.findByNameAndType(tagName, type)
                        .orElseGet(() -> supplementTagRepository.save(new SupplementTag(tagName, type)));

                SupplementReviewTag reviewTag = new SupplementReviewTag();
                reviewTag.setReview(review);
                reviewTag.setTag(tag);
                supplementReviewTagRepository.save(reviewTag);
            }
        });

        return review.getId();
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(Long userId, Long reviewId) {
        SupplementReview review = supplementReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다"));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다");
        }

        supplementReviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<UserSupplementReviewResponse> getReviewsByUser(Long userId) {
        List<SupplementReview> reviews = supplementReviewRepository.findByUserId(userId);
        List<Long> reviewIds = reviews.stream().map(SupplementReview::getId).toList();

        // 좋아요 여부 최적화 조회
        List<Long> likedIds = supplementReviewLikeRepository.findLikedReviewIds(userId, reviewIds);
        Set<Long> likedIdSet = new HashSet<>(likedIds);

        return reviews.stream()
                .map(r -> new UserSupplementReviewResponse(
                        r.getSupplement().getId(),
                        r.getSupplement().getProductName(),
                        SupplementReviewResponse.from(r, userId, likedIdSet.contains(r.getId()))
                ))
                .toList();
    }



    @Transactional(readOnly = true)
    public Page<SupplementReviewResponse> getPagedReviews(Long supplementId, Long userId, Pageable pageable) {
        // 1. 기본 리뷰 페이징 + 정렬
        Page<SupplementReview> page = supplementReviewRepository.findBySupplementId(supplementId, pageable);
        List<SupplementReview> reviews = page.getContent();

        // 2. 좋아요 여부 최적화 조회
        List<Long> reviewIds = reviews.stream().map(SupplementReview::getId).toList();
        List<Long> likedIds = supplementReviewLikeRepository.findLikedReviewIds(userId, reviewIds);
        Set<Long> likedIdSet = new HashSet<>(likedIds);

        // 3. DTO 변환 + isLiked 적용
        List<SupplementReviewResponse> responseList = reviews.stream()
                .map(r -> SupplementReviewResponse.from(r, userId, likedIdSet.contains(r.getId())))
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
        List<SupplementReview> reviews = supplementReviewRepository.findBySupplementIdWithReviewTags(drugId); // 단일 호출

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

    public RatingStatsResponse computeRatingStatsFromReviews(List<SupplementReview> reviews) {
        double avg = reviews.stream()
                .mapToDouble(SupplementReview::getRating)
                .average().orElse(0.0);

        Map<Integer, Long> buckets = IntStream.rangeClosed(1, 5)
                .boxed().collect(Collectors.toMap(i -> i, i -> 0L));

        for (SupplementReview r : reviews) {
            int b = (int) Math.floor(r.getRating());
            buckets.put(b, buckets.get(b) + 1);
        }

        return new RatingStatsResponse(avg, buckets);
    }

    public Map<TagType, TagStatsDto> computeTagStatsFromReviews(List<SupplementReview> reviews) {
        Map<TagType, Map<String, Long>> grouped = new EnumMap<>(TagType.class);

        for (SupplementReview r : reviews) {
            for (SupplementReviewTag rt : r.getReviewTags()) {
                SupplementTag tag = rt.getTag();
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

