package com.vanvu.phoneshop.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.Review;
import com.vanvu.phoneshop.service.ProductService;
import com.vanvu.phoneshop.service.OrderService;


import com.vanvu.phoneshop.service.ReviewService;
import com.vanvu.phoneshop.model.User;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @PostMapping("/user/review/add")
    public String addReview(@RequestParam("productID") String productID,
                            @RequestParam("rating") int rating,
                            @RequestParam("comment") String comment,
                            HttpSession session,
                            RedirectAttributes ra) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        // check user đã mua sản phẩm chưa
        boolean hasPurchased = orderService.hasUserPurchasedProduct(user.getUserID(), productID);
        if(!hasPurchased) {
            ra.addFlashAttribute("errorReview", "Bạn cần mua sản phẩm thành công mới có thể đánh giá");
            return "redirect:/user/product/" + productID + "#review-section";
        }

        Review review = new Review();
        review.setProduct(productService.getProductById(productID));
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsVisible(true);
        review.setCreatedDate(LocalDateTime.now());
        
        reviewService.saveReview(review);

        ra.addFlashAttribute("successReview", "Cảm ơn bạn đã đánh giá sản phẩm");
        return "redirect:/user/product/" + productID + "#review-section";
    }


}