package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.User;
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
public interface UserRepository extends JpaRepository <User, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the users table


    // CREATE - Insert a new user
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a user
    """)
    void saveUser(String name, String email, String password);


    // READ - Find all users
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all users
    """)
    List<User> findAllUsers();


    // READ - Find user by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a user by ID
    """)
    Optional<User> findUserById(@Param("userId") int userId);


    // READ - Find user by email
    @Query(value = """
            SELECT *
            FROM users
            WHERE email = :email
            """, nativeQuery = true)
    Optional<User> findUserByEmail(@Param("email") String email);


    // UPDATE - Update a user by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a user by ID
    """)
    void updateUser(int userId, String name, String email, String password);


    // DELETE - Delete a user by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a user by ID
    """)
    void deleteUserById(@Param("userId") int userId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
