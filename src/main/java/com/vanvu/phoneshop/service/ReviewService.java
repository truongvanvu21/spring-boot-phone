package com.vanvu.phoneshop.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.vanvu.phoneshop.repository.ReviewRepository;
import com.vanvu.phoneshop.model.Review;
import org.springframework.stereotype.Service;
import java.util.List;

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
}
