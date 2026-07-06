package com.rocketFoodDelivery.rocketFood.repository;


import com.rocketFoodDelivery.rocketFood.models.Product;

// Spring Framework
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
public interface ProductRepository extends JpaRepository <Product, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the products table


    // CREATE - Insert a new product
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a product
    """)
    void saveProduct(int restaurantId, String name, String description, int cost);


    // READ - Find all products
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all products
    """)
    List<Product> findAllProducts();


    // READ - Find product by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a product by ID
    """)
    Optional<Product> findProductById(@Param("productId") int productId);


    // READ - Find products by restaurant ID
    @Query(value = """
            SELECT *
            FROM products
            WHERE restaurant_id = :restaurantId
            """, nativeQuery = true)
    List<Product> findProductsByRestaurantId(@Param("restaurantId") int restaurantId);


    // UPDATE - Update a product by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a product by ID
    """)
    void updateProduct(int productId, String name, String description, int cost);


    // DELETE - Delete a product by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a product by ID
    """)
    void deleteProductById(@Param("productId") int productId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
