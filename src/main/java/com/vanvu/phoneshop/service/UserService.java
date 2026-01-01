package com.vanvu.phoneshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vanvu.phoneshop.repository.UserRepository;
import com.vanvu.phoneshop.model.User;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Đăng nhập - kiểm tra email và password
    public User login(String email, String password) {       
        return userRepository.findByEmailAndPassword(email, password);
    }

    // Lấy user theo email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
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
