package com.vanvu.phoneshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vanvu.phoneshop.repository.ProductRepository;
import com.vanvu.phoneshop.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    private final String uploadDir = "src/main/resources/static/uploads/products/";

    // Lấy tất cả sản phẩm kèm sắp xếp
    public List<Product> getAllProducts(Sort sort) {
        return productRepository.findAll(sort);
    }

    // Lấy tất cả sản phẩm với phân trang
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // Đếm tổng số sản phẩm
    public long countProducts() {
        return productRepository.count();
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

    // Tìm kiếm sản phẩm với phân trang
    public Page<Product> searchProducts(String keyword, String categoryID, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty() && categoryID != null && !categoryID.isEmpty()) {
            return productRepository.findByProductNameContainingIgnoreCaseAndCategoryCategoryID(keyword, categoryID, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
        } else if (categoryID != null && !categoryID.isEmpty()) {
            return productRepository.findByCategoryCategoryID(categoryID, pageable);
        }
        return productRepository.findAll(pageable);
    }

    // Lấy sản phẩm sắp hết hàng 
    public List<Product> getLowStockProducts(int quantity) {
        return productRepository.findByQuantityLessThanEqualOrderByQuantityAsc(quantity);
    }

    // Lưu sản phẩm mới
    public Product saveProduct(Product product) {
        if (product.getCreatedDate() == null || product.getCreatedDate().isEmpty()) {
            product.setCreatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return productRepository.save(product);
    }

    // Xóa sản phẩm
    public void deleteProduct(String productID) {
        Product product = getProductById(productID);
        if (product != null) {
            // Xóa file ảnh nếu có
            if (product.getBaseImage() != null && !product.getBaseImage().isEmpty()) {
                deleteImage(product.getBaseImage());
            }
            productRepository.deleteById(productID);
        }
    }

    // Kiểm tra mã sản phẩm đã tồn tại
    public boolean existsById(String productID) {
        return productRepository.existsById(productID);
    }

    // Upload ảnh sản phẩm
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return newFilename;
    }

    // Xóa ảnh
    public void deleteImage(String filename) {
        try {
            Path filePath = Paths.get(uploadDir + filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
