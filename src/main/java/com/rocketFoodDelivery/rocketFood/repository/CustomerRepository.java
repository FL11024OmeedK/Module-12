package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.Customer;
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
public interface CustomerRepository extends JpaRepository <Customer, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the customers table


    // CREATE - Insert a new customer
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a customer
    """)
    void saveCustomer(int userId, int addressId, String phone, String email);


    // READ - Find all customers
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all customers
    """)
    List<Customer> findAllCustomers();


    // READ - Find customer by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a customer by ID
    """)
    Optional<Customer> findCustomerById(@Param("customerId") int customerId);


    // READ - Find customer by user ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a customer by ID
    """)
    Optional<Customer> findCustomerByUserId(@Param("userId") int userId);


    // UPDATE - Update a customer by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a customer by ID
    """)
    void updateCustomer(int customerId, String phone, String email, boolean active);


    // DELETE - Delete a customer by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a customer by ID
    """)
    void deleteCustomerById(@Param("customerId") int customerId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
