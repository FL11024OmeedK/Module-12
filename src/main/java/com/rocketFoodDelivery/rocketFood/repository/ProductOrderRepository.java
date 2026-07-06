package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.ProductOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


// Java standard library
import java.util.List;
import java.util.Optional;


// Repository interface that provides CRUD operations for database access
@Repository
public interface ProductOrderRepository extends JpaRepository <ProductOrder, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the productOrders table


    // CREATE - Insert a new productOrder
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a productOrder
    """)
    void saveProductOrder(int productId, int orderId, int productQuantity, int productUnitCost);


    // READ - Find all productOrders
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all productOrders
    """)
    List<ProductOrder> findAllProductOrders();


    // READ - Find productOrder by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a productOrder by ID
    """)
    Optional<ProductOrder> findProductOrderById(@Param("productOrderId") int productOrderId);


    // UPDATE - Update a productOrder by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a productOrder by ID
    """)
    void updateProductOrder(int productOrderId, int productQuantity, int productUnitCost);


    // DELETE - Delete a productOrder by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a productOrder by ID
    """)
    void deleteProductOrderById(@Param("productOrderId") int productOrderId);


    // DELETE - Delete a productOrder by order ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a productOrder by order ID
    """)
    void deleteProductOrdersByOrderId(@Param("orderId") int orderId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();


    // ==================== Native SQL Business Rule Helper Queries ====================

    // COUNT - Check for duplicate product in the same order (excluding a specific ID)
    @Query(value = """
            SELECT COUNT(*) FROM product_orders
            WHERE order_id = :orderId AND product_id = :productId AND id != :excludeId
            """, nativeQuery = true)
    int countDuplicateProductOrder(@Param("productId") int productId, @Param("orderId") int orderId, @Param("excludeId") int excludeId);

    // READ - Get restaurant_id from a product
    @Query(value = """
            SELECT restaurant_id FROM products WHERE id = :productId
            """, nativeQuery = true)
    Integer findRestaurantIdByProductId(@Param("productId") int productId);

    // READ - Get restaurant_id from an order
    @Query(value = """
            SELECT restaurant_id FROM orders WHERE id = :orderId
            """, nativeQuery = true)
    Integer findRestaurantIdByOrderId(@Param("orderId") int orderId);
}
