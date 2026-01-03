package com.vanvu.phoneshop.repository;

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
}
