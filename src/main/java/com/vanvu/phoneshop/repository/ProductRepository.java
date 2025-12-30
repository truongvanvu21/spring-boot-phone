package com.vanvu.phoneshop.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vanvu.phoneshop.model.Product;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategoryID(String categoryID);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);
}
