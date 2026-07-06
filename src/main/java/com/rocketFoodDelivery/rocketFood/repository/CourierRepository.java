package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.Courier;
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
public interface CourierRepository extends JpaRepository <Courier, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the couriers table


    // CREATE - Insert a new courier
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a courier
    """)
    void saveCourier(int userId, int addressId, int courierStatusId, String phone, String email);


    // READ - Find all couriers
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all couriers
    """)
    List<Courier> findAllCouriers();


    // READ - Find courier by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a courier by ID
    """)
    Optional<Courier> findCourierById(@Param("courierId") int courierId);


    // READ - Find courier by user ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a courier by ID
    """)
    Optional<Courier> findCourierByUserId(@Param("userId") int userId);


    // UPDATE - Update a courier by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a courier by ID
    """)
    void updateCourier(int courierId, int courierStatusId, String phone, String email, boolean active);


    // DELETE - Delete a courier by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a courier by ID
    """)
    void deleteCourierById(@Param("courierId") int courierId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
