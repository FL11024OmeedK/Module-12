package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.Address;
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
public interface AddressRepository extends JpaRepository<Address, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the addresses table


    // CREATE - Insert a new address
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save an address
    """)
    void saveAddress(String streetAddress, String city, String postalCode);


    // READ - Find all addresses
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all addresses
    """)
    List<Address> findAllAddresses();


    // READ - Find address by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find an address by ID
    """)
    Optional<Address> findAddressById(@Param("addressId") int addressId);


    // UPDATE - Update an address by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update an address by ID
    """)
    void updateAddress(int addressId, String streetAddress, String city, String postalCode);


    // DELETE - Delete an address by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete an address by ID
    """)
    void deleteAddressById(@Param("addressId") int addressId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
