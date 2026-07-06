package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
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
public interface OrderStatusRepository extends JpaRepository <OrderStatus, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the orderStatuses table

    
    // CREATE - Insert a new orderStatus
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save an orderStatus
    """)
    void saveOrderStatus(String name);


    // READ - Find all orderStatuses
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all orderStatuses
    """)
    List<OrderStatus> findAllOrderStatuses();


    // READ - Find orderStatus by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find an orderStatus by ID
    """)
    Optional<OrderStatus> findOrderStatusById(@Param("orderStatusId") int orderStatusId);


    // READ - Find orderStatus by name
    @Query(value = """
            SELECT *
            FROM order_statuses
            WHERE name = :name
            """, nativeQuery = true)
    Optional<OrderStatus> findOrderStatusByName(@Param("name") String name);


    // UPDATE - Update an orderStatus by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update an orderStatus by ID
    """)
    void updateOrderStatus(int orderStatusId, String name);


    // DELETE - Delete an orderStatus by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete an orderStatus by ID
    """)
    void deleteOrderStatusById(@Param("orderStatusId") int orderStatusId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
