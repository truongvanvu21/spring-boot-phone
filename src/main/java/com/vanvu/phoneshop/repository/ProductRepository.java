package com.vanvu.phoneshop.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vanvu.phoneshop.model.Category;
import com.vanvu.phoneshop.model.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // Tìm sản phẩm theo Category entity
    List<Product> findByCategory(Category category);
    
    // Tìm sản phẩm theo categoryID 
    // List<Product> findByCategoryCategoryID(String categoryID);

    // Tìm sản phẩm theo tên
    // List<Product> findByProductNameContainingIgnoreCase(String keyword);

    // Tìm sản phẩm theo Category entity với sắp xếp
    List<Product> findByCategoryCategoryID(String categoryID, Sort sort);

    // Tìm sản phẩm theo tên với sắp xếp
    List<Product> findByProductNameContainingIgnoreCase(String keyword, Sort sort);

    // Lấy sản phẩm có số lượng thấp (sắp hết hàng)
    List<Product> findByQuantityLessThanEqualOrderByQuantityAsc(int quantity);

    // Tìm kiếm theo tên với phân trang
    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Tìm theo category với phân trang
    Page<Product> findByCategoryCategoryID(String categoryID, Pageable pageable);

    // Tìm theo tên và category với phân trang
    Page<Product> findByProductNameContainingIgnoreCaseAndCategoryCategoryID(String keyword, String categoryID, Pageable pageable);
}
