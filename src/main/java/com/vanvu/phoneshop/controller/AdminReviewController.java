package com.vanvu.phoneshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vanvu.phoneshop.model.Review;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.service.ReviewService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    // Kiểm tra quyền admin
    private boolean isAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        return loggedInUser != null && loggedInUser.getRole() == 1;
    }

    // Hiển thị danh sách đánh giá
    @GetMapping("/reviews")
    public String listReviews(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean replied,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Page<Review> reviewPage = reviewService.searchReviews(keyword, rating, replied, page, size);

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("rating", rating);
        model.addAttribute("replied", replied);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("totalElements", reviewPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("pendingReply", reviewService.countPendingReply());

        return "admin/reviews";
    }

    // Xem chi tiết đánh giá
    @GetMapping("/reviews/detail/{reviewID}")
    public String reviewDetail(@PathVariable Integer reviewID, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Review review = reviewService.getReviewById(reviewID);
        if (review == null) {
            return "redirect:/admin/reviews";
        }

        model.addAttribute("review", review);
        return "admin/review-detail";
    }

    // Trả lời đánh giá
    @PostMapping("/reviews/reply/{reviewID}")
    public String replyReview(
            @PathVariable Integer reviewID,
            @RequestParam String adminReply,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            reviewService.replyReview(reviewID, adminReply);
            redirectAttributes.addFlashAttribute("success", "Trả lời đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/reviews/detail/" + reviewID;
    }

    // Ẩn đánh giá (vi phạm)
    @PostMapping("/reviews/hide/{reviewID}")
    public String hideReview(
            @PathVariable Integer reviewID,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            reviewService.hideReview(reviewID);
            redirectAttributes.addFlashAttribute("success", "Đã ẩn đánh giá vi phạm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/reviews";
    }

    // Khôi phục đánh giá
    @PostMapping("/reviews/restore/{reviewID}")
    public String restoreReview(
            @PathVariable Integer reviewID,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            reviewService.restoreReview(reviewID);
            redirectAttributes.addFlashAttribute("success", "Đã khôi phục đánh giá!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/reviews";
    }

    // Xóa hoàn toàn đánh giá
    @PostMapping("/reviews/delete/{reviewID}")
    public String deleteReview(
            @PathVariable Integer reviewID,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        try {
            reviewService.deleteReview(reviewID);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/reviews";
    }
}
