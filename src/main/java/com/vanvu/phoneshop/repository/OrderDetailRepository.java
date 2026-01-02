package com.vanvu.phoneshop.repository;

import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.OrderDetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    
    // Tìm chi tiết đơn hàng theo Order
    List<OrderDetail> findByOrder(Order order);
    
    // Tìm chi tiết đơn hàng theo OrderID
    List<OrderDetail> findByOrderOrderID(Integer orderID);
    
    // Xóa chi tiết đơn hàng theo OrderID
    void deleteByOrderOrderID(Integer orderID);
}
