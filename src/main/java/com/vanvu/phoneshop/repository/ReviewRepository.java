package com.vanvu.phoneshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.vanvu.phoneshop.model.Review;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT r FROM Review r WHERE r.product.productID = :productID AND r.isVisible = true ORDER BY r.createdDate DESC")
    List<Review> getReviewByProduct(@Param("productID") String productID);

    // Lấy tất cả đánh giá với phân trang và tìm kiếm
    @Query("SELECT r FROM Review r " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR r.product.productName LIKE %:keyword% OR r.user.fullName LIKE %:keyword% OR r.comment LIKE %:keyword%) " +
           "AND (:rating IS NULL OR r.rating = :rating) " +
           "AND (:replied IS NULL OR (:replied = true AND r.adminReply IS NOT NULL) OR (:replied = false AND r.adminReply IS NULL)) " +
           "ORDER BY r.createdDate DESC")
    Page<Review> searchReviews(@Param("keyword") String keyword, 
                               @Param("rating") Integer rating,
                               @Param("replied") Boolean replied,
                               Pageable pageable);

    // Đếm tổng số đánh giá
    long count();

    // Đếm số đánh giá chưa trả lời
    @Query("SELECT COUNT(r) FROM Review r WHERE r.adminReply IS NULL")
    long countPendingReply();

    // Tính rating trung bình của sản phẩm
    @Query("SELECT COALESCE(AVG(r.rating), 5.0) FROM Review r WHERE r.product.productID = :productID AND r.isVisible = true")
    Double getAverageRatingByProductId(@Param("productID") String productID);

    // Đếm số lượng đánh giá của sản phẩm
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productID = :productID AND r.isVisible = true")
    Long countReviewsByProductId(@Param("productID") String productID);
}
