package com.vanvu.phoneshop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vanvu.phoneshop.model.Review;
import com.vanvu.phoneshop.repository.ReviewRepository;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    public List<Review> getVisibleReviewsByProduct(String productID) {
        return reviewRepository.getReviewByProduct(productID);
    }

    // Lấy đánh giá theo ID
    public Review getReviewById(Integer reviewID) {
        return reviewRepository.findById(reviewID).orElse(null);
    }

    // Tìm kiếm đánh giá với phân trang
    public Page<Review> searchReviews(String keyword, Integer rating, Boolean replied, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return reviewRepository.searchReviews(searchKeyword, rating, replied, pageable);
    }

    // Trả lời đánh giá
    public void replyReview(Integer reviewID, String adminReply) {
        Review review = reviewRepository.findById(reviewID).orElse(null);
        if (review != null) {
            review.setAdminReply(adminReply);
            review.setRepliedDate(LocalDateTime.now());
            reviewRepository.save(review);
        }
    }

    // Xóa đánh giá (ẩn đánh giá)
    public void hideReview(Integer reviewID) {
        Review review = reviewRepository.findById(reviewID).orElse(null);
        if (review != null) {
            review.setIsVisible(false);
            reviewRepository.save(review);
        }
    }

    // Xóa hoàn toàn đánh giá
    public void deleteReview(Integer reviewID) {
        reviewRepository.deleteById(reviewID);
    }

    // Khôi phục đánh giá
    public void restoreReview(Integer reviewID) {
        Review review = reviewRepository.findById(reviewID).orElse(null);
        if (review != null) {
            review.setIsVisible(true);
            reviewRepository.save(review);
        }
    }

    // Đếm tổng số đánh giá
    public long countReviews() {
        return reviewRepository.count();
    }

    // Đếm số đánh giá chưa trả lời
    public long countPendingReply() {
        return reviewRepository.countPendingReply();
    }

    // Lấy rating trung bình của sản phẩm (mặc định 5 nếu chưa có đánh giá)
    public Double getAverageRating(String productID) {
        Double avg = reviewRepository.getAverageRatingByProductId(productID);
        return avg != null ? avg : 5.0;
    }

    // Đếm số lượng đánh giá của sản phẩm
    public Long countReviewsByProduct(String productID) {
        return reviewRepository.countReviewsByProductId(productID);
    }
}
