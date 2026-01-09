package com.vanvu.phoneshop.repository;

import com.vanvu.phoneshop.dto.MonthlyRevenueDTO;
import com.vanvu.phoneshop.dto.TopBrandDTO;
import com.vanvu.phoneshop.dto.TopSellingProductDTO;
import com.vanvu.phoneshop.model.Order;
import com.vanvu.phoneshop.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // Tìm đơn hàng theo User
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    // Tìm đơn hàng theo UserID
    List<Order> findByUserUserIDOrderByOrderDateDesc(Integer userID);
    
    // Tìm đơn hàng theo trạng thái
    List<Order> findByStatus(Integer status);

    // Tìm đơn hàng theo trạng thái sắp xếp theo ngày giảm dần
    List<Order> findByStatusOrderByOrderDateDesc(Integer status);

    // Tìm kiếm đơn hàng theo trạng thái và keyword (mã đơn hoặc tên khách hàng)
    @Query("SELECT o FROM Order o WHERE o.status = :status AND " +
           "(CAST(o.orderID AS string) LIKE %:keyword% OR o.user.fullName LIKE %:keyword% OR o.user.email LIKE %:keyword%) " +
           "ORDER BY o.orderDate DESC")
    List<Order> searchByStatusAndKeyword(@Param("status") Integer status, @Param("keyword") String keyword);

    // Tìm kiếm đơn hàng với phân trang và lọc theo ngày
    @Query("SELECT o FROM Order o WHERE o.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR CAST(o.orderID AS string) LIKE %:keyword% OR o.user.fullName LIKE %:keyword% OR o.user.email LIKE %:keyword%) " +
           "AND (:fromDate IS NULL OR o.orderDate >= :fromDate) " +
           "AND (:toDate IS NULL OR o.orderDate <= :toDate) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> searchOrdersWithFilters(
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    // Đếm số đơn hàng với bộ lọc
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR CAST(o.orderID AS string) LIKE %:keyword% OR o.user.fullName LIKE %:keyword% OR o.user.email LIKE %:keyword%) " +
           "AND (:fromDate IS NULL OR o.orderDate >= :fromDate) " +
           "AND (:toDate IS NULL OR o.orderDate <= :toDate)")
    long countOrdersWithFilters(
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    // Đếm số đơn hàng theo trạng thái
    long countByStatus(Integer status);

    // Tìm đơn hàng theo User và trạng thái
    List<Order> findByUserAndStatus(User user, Integer status);

    // Tính doanh thu tháng hiện tại (các đơn đã được admin xác nhận)
    @Query("SELECT COALESCE(SUM(od.quantity * od.product.price), 0.0) " +
           "FROM Order o JOIN o.orderDetails od " +
           "WHERE o.status = 2 " +
           "AND FUNCTION('MONTH', o.orderDate) = FUNCTION('MONTH', CURRENT_DATE) " +
           "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Double getMonthlyRevenue();

    // Check user đã mua sản phẩm này chưa (để cho review)
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderDetails od "
        + "WHERE o.user.userID = :userID AND od.product.productID = :productID AND o.status = 2")
    boolean hasPurchasedProduct(@Param("userID") Integer userID, @Param("productID") String productID);

    // Lấy doanh thu 12 tháng của năm
    @Query("SELECT new com.vanvu.phoneshop.dto.MonthlyRevenueDTO(" +
           "CAST(FUNCTION('MONTH', o.orderDate) AS integer), " +
           "CAST(COALESCE(SUM(od.quantity * od.product.price), 0.0) AS double)) " +
           "FROM Order o JOIN o.orderDetails od " +
           "WHERE o.status = 2 AND FUNCTION('YEAR', o.orderDate) = :year " +
           "GROUP BY FUNCTION('MONTH', o.orderDate) " +
           "ORDER BY FUNCTION('MONTH', o.orderDate)")
    List<MonthlyRevenueDTO> getMonthlyRevenueByYear(@Param("year") int year);

    // Lấy Top sản phẩm bán chạy nhất
    @Query("SELECT new com.vanvu.phoneshop.dto.TopSellingProductDTO(" +
           "od.product.productID, od.product.productName, od.product.baseImage, " +
           "CAST(SUM(od.quantity) AS long), CAST(SUM(od.quantity * od.product.price) AS double)) " +
           "FROM Order o JOIN o.orderDetails od " +
           "WHERE o.status = 2 " +
           "GROUP BY od.product.productID, od.product.productName, od.product.baseImage " +
           "ORDER BY SUM(od.quantity) DESC")
    List<TopSellingProductDTO> getTopSellingProducts();

    // Lấy Top 5 hãng (Category) bán chạy nhất
    @Query("SELECT new com.vanvu.phoneshop.dto.TopBrandDTO(" +
           "od.product.category.categoryID, od.product.category.categoryName, " +
           "CAST(SUM(od.quantity) AS long)) " +
           "FROM Order o JOIN o.orderDetails od " +
           "WHERE o.status = 2 " +
           "GROUP BY od.product.category.categoryID, od.product.category.categoryName " +
           "ORDER BY SUM(od.quantity) DESC")
    List<TopBrandDTO> getTopSellingBrands();
}
