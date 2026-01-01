package com.vanvu.phoneshop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vanvu.phoneshop.dto.CartItemDTO;
import com.vanvu.phoneshop.repository.ProductRepository;
import com.vanvu.phoneshop.repository.UserRepository;
import com.vanvu.phoneshop.repository.CartItemRepository;
import com.vanvu.phoneshop.repository.CartRepository;
import com.vanvu.phoneshop.model.Cart;
import com.vanvu.phoneshop.model.Product;
import com.vanvu.phoneshop.model.User;
import com.vanvu.phoneshop.model.CartItem;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(Integer userID, String productID, Integer quantity) {
        // Lấy User
        User user = userRepository.findById(userID).orElse(null);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        // Lấy giỏ hàng của user, nếu chưa có thì tạo mới
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCreatedDate(LocalDateTime.now());
            cart = cartRepository.save(cart);
        }

        // Lấy Product
        Product product = productRepository.findById(productID).orElse(null);
        if (product == null) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        // Kiểm tra nếu sản phẩm đã có trong giỏ hàng thì cập nhật số lượng
        CartItem existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
        cart.setUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
    }

    // Lấy danh sách sản phẩm trong giỏ hàng
    public List<CartItemDTO> getCartItems(Integer userID) {
        return cartItemRepository.findCartItemsByUserID(userID);
    }

    // Lấy tổng số lượng sản phẩm trong giỏ
    public int getTotalItems(Integer userID) {
        Cart cart = cartRepository.findByUserUserID(userID);
        if (cart == null) {
            return 0;
        }

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    // Lấy tổng tiền giỏ hàng
    public double getTotalAmount(Integer userID) {
        List<CartItemDTO> items = getCartItems(userID);
        double total = 0;
        for (CartItemDTO item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    // Cập nhật số lượng sản phẩm
    public void updateCartItem(Integer userID, String productID, Integer quantity) {
        Cart cart = cartRepository.findByUserUserID(userID);
        if (cart == null) {
            throw new RuntimeException("Giỏ hàng không tồn tại");
        }

        Product product = productRepository.findById(productID).orElse(null);
        if (product == null) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product);
        if (item == null) {
            throw new RuntimeException("Sản phẩm không có trong giỏ hàng");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cart.setUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeCartItem(Integer userID, String productID) {
        Cart cart = cartRepository.findByUserUserID(userID);
        if (cart == null) {
            throw new RuntimeException("Giỏ hàng không tồn tại");
        }

        Product product = productRepository.findById(productID).orElse(null);
        if (product == null) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product);
        if (item != null) {
            cartItemRepository.delete(item);
            cart.setUpdatedDate(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    // Xóa tất cả sản phẩm trong giỏ hàng
    public void clearCart(Integer userID) {
        Cart cart = cartRepository.findByUserUserID(userID);
        if (cart == null) {
            return;
        }

        List<CartItem> items = cartItemRepository.findByCart(cart);
        cartItemRepository.deleteAll(items);

        cart.setUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
