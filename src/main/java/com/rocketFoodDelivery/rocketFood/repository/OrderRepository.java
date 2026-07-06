package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.Order;
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
public interface OrderRepository extends JpaRepository <Order, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the orders table


    // CREATE - Insert a new order
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save an order
    """)
    void saveOrder(int restaurantId, int customerId, int orderStatusId);


    // READ - Find all orders
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all orders
    """)
    List<Order> findAllOrders();


    // READ - Find order by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find an order by ID
    """)
    Optional<Order> findOrderById(@Param("orderId") int orderId);


    // READ - Find orders by restaurant ID
    @Query(value = """            
            // todo: Add Native SQL query to find an order by restaurant ID
            """, nativeQuery = true)
    List<Order> findOrdersByRestaurantId(@Param("restaurantId") int restaurantId);

    // READ - Find orders by customer ID
    @Query(value = """            
            // todo: Add Native SQL query to find an order by customer ID
            """, nativeQuery = true)
    List<Order> findOrdersByCustomerId(@Param("customerId") int customerId);

    // READ - Find orders by courier ID
    @Query(value = """            
            // todo: Add Native SQL query to find an order by courier ID
            """, nativeQuery = true)
    List<Order> findOrdersByCourierId(@Param("courierId") int courierId);


    // UPDATE - Update an order by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update an order by ID
    """)
    void updateOrder(int orderId, int orderStatusId, Integer restaurantRating);


    // UPDATE - Update only order status
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE orders SET order_status_id = :orderStatusId WHERE id = :id
            """, nativeQuery = true)
    void updateOrderStatus(@Param("id") int id, @Param("orderStatusId") int orderStatusId);


    // DELETE - Delete an order by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete an order by ID
    """)
    void deleteOrderById(@Param("orderId") int orderId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
