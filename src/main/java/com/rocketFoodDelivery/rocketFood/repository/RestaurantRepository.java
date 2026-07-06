package com.rocketFoodDelivery.rocketFood.repository;


// Project models
import com.rocketFoodDelivery.rocketFood.models.Restaurant;


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
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the products table


    // Custom query to find a restaurant by its ID along with its average rating
    @Query(nativeQuery = true, value = """
        SELECT r.id, r.name, r.price_range, COALESCE(CEIL(SUM(o.restaurant_rating) / NULLIF(COUNT(o.id), 0)), 0) AS rating
        FROM restaurants r
        LEFT JOIN orders o ON r.id = o.restaurant_id
        WHERE r.id = :restaurantId
        GROUP BY r.id
    """)
    List<Object[]> findRestaurantWithAverageRatingById(@Param("restaurantId") int restaurantId);
    
    // Custom query to find restaurants by rating and price range
    @Query(nativeQuery = true, value = """
        SELECT * FROM (
        SELECT r.id, r.name, r.price_range, COALESCE(CEIL(SUM(o.restaurant_rating) / NULLIF(COUNT(o.id), 0)), 0) AS rating
        FROM restaurants r
        LEFT JOIN orders o ON r.id = o.restaurant_id
        WHERE (:priceRange IS NULL OR r.price_range = :priceRange)
        GROUP BY r.id
        ) AS result
        WHERE (:rating IS NULL OR result.rating = :rating)
    """)
    List<Object[]> findRestaurantsByRatingAndPriceRange(@Param("rating") Integer rating, @Param("priceRange") Integer priceRange);


    
    // CREATE - Insert a new restaurant
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save a restaurant
    """)
    void saveRestaurant(long userId, long addressId, String name, int priceRange, String phone, String email);


    // READ - Find all restaurants
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all restaurants
    """)
    List<Restaurant> findAllRestaurants();


    // READ - Find restaurant by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find a restaurant by ID
    """)
    Optional<Restaurant> findRestaurantById(@Param("restaurantId") int restaurantId);


    // UPDATE - Update a restaurant by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update a restaurant by ID
    """)
    void updateRestaurant(int restaurantId, String name, int priceRange, String phone);


    // DELETE - Delete a restaurant by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete a restaurant by ID
    """)
    void deleteRestaurantById(@Param("restaurantId") int restaurantId);
    

    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        SELECT LAST_INSERT_ID() AS id
    """)
    int getLastInsertedId();
}
    










    




