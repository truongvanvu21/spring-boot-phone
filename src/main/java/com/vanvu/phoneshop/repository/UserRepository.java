package com.vanvu.phoneshop.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
