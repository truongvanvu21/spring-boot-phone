package com.vanvu.phoneshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vanvu.phoneshop.repository.ProductRepository;
import com.vanvu.phoneshop.model.Product;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    // Lấy tất cả sản phẩm kèm sắp xếp
    public List<Product> getAllProducts(Sort sort) {
        return productRepository.findAll(sort);
    }

    // Lấy theo hãng kèm sắp xếp
    public List<Product> getProductsByCategoryID(String categoryID, Sort sort) {
        return productRepository.findByCategoryCategoryID(categoryID, sort);
    }

    public Product getProductById(String productID) {
        return productRepository.findById(productID).orElse(null);
    }

    // Tìm kiếm theo tên kèm sắp xếp
    public List<Product> searchProductsByName(String keyword, Sort sort) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword, sort);
    }
}
