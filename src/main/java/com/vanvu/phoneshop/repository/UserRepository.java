package com.vanvu.phoneshop.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vanvu.phoneshop.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm user theo email và password (dùng cho login)
    User findByEmailAndPassword(String email, String password);
    
    // Tìm user theo email
    User findByEmail(String email);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // Đếm số user theo role
    long countByRole(int role);

    // Tìm kiếm khách hàng (role = 0) theo tên hoặc email với phân trang
    @Query("SELECT u FROM User u WHERE u.role = 0 " +
           "AND (:keyword IS NULL OR :keyword = '' OR u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%) " +
           "ORDER BY u.userID DESC")
    Page<User> searchCustomers(@Param("keyword") String keyword, Pageable pageable);
}
