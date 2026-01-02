package com.vanvu.phoneshop.repository;

import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // Tìm đơn hàng theo User
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    // Tìm đơn hàng theo UserID
    List<Order> findByUserUserIDOrderByOrderDateDesc(Integer userID);
    
    // Tìm đơn hàng theo trạng thái
    List<Order> findByStatus(Integer status);
    
    // Tìm đơn hàng theo User và trạng thái
    List<Order> findByUserAndStatus(User user, Integer status);
}
