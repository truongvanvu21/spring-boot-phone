package com.vanvu.phoneshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vanvu.phoneshop.repository.ProductRepository;
import com.vanvu.phoneshop.model.Product;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategoryID(String categoryID) {
        return productRepository.findByCategoryID(categoryID);
    }

    public Product getProductById(String productID) {
        return productRepository.findById(productID).orElse(null);
    }

    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword);
    }
}
