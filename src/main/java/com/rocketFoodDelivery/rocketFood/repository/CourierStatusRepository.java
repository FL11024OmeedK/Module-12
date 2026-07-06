package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.CourierStatus;
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
public interface CourierStatusRepository extends JpaRepository <CourierStatus, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the courierStatuses table


    // CREATE - Insert a new courierStatus
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a courierStatus
    """)
    void saveCourierStatus(String name);


    // READ - Find all courierStatuses
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all courierStatuses
    """)
    List<CourierStatus> findAllCourierStatuses();


    // READ - Find courierStatus by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a courierStatus by ID
    """)
    Optional<CourierStatus> findCourierStatusById(@Param("courierStatusId") int courierStatusId);


    // UPDATE - Update a courierStatus by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a courierStatus by ID
    """)
    void updateCourierStatus(int courierStatusId, String name);


    // DELETE - Delete a courierStatus by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a courierStatus by ID
    """)
    void deleteCourierStatusById(@Param("courierStatusId") int courierStatusId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
