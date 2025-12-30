package com.vanvu.phoneshop.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vanvu.phoneshop.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm user theo email và password (dùng cho login)
    Optional<User> findByEmailAndPassword(String email, String password);
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
}
