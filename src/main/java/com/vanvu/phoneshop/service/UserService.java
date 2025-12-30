package com.vanvu.phoneshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vanvu.phoneshop.repository.UserRepository;
import com.vanvu.phoneshop.model.User;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Đăng nhập - kiểm tra email và password
    public User login(String email, String password) {       
        Optional<User> user = userRepository.findByEmailAndPassword(email, password);        
        return user.orElse(null);
    }

    // Lấy user theo email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Lưu user mới
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
