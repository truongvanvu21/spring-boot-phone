package com.vanvu.phoneshop.repository;

import com.vanvu.phoneshop.dto.CartItemDTO;
import com.vanvu.phoneshop.model.Cart;
import com.vanvu.phoneshop.model.CartItem;
import com.vanvu.phoneshop.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // Tìm tất cả CartItem theo Cart entity
    List<CartItem> findByCart(Cart cart);
    
    // Tìm CartItem theo Cart và Product entity
    CartItem findByCartAndProduct(Cart cart, Product product);

    // Lấy danh sách giỏ hàng theo UserID
    @Query("SELECT new com.vanvu.phoneshop.dto.CartItemDTO(" +
           "ci.product.productID, ci.product.productName, ci.product.baseImage, ci.product.price, ci.quantity) " +
           "FROM CartItem ci " +
           "WHERE ci.cart.user.userID = :userID")
    List<CartItemDTO> findCartItemsByUserID(@Param("userID") Integer userID);
}