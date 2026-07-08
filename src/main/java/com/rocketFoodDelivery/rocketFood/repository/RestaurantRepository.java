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


    // ==================== Native SQL Rating Queries (provided) ====================

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


    // ==================== Native SQL CRUD Queries ====================

    // CREATE - Insert a new restaurant.
    // Positional binding (?1..?6); no active parameter, so new restaurants are always active = true.
    // created_on / update_on set with NOW() (native SQL bypasses @CreationTimestamp).
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        INSERT INTO restaurants (user_id, address_id, name, price_range, phone, email, active, created_on, update_on)
        VALUES (?1, ?2, ?3, ?4, ?5, ?6, true, NOW(), NOW())
    """)
    void saveRestaurant(long userId, long addressId, String name, int priceRange, String phone, String email);


    // READ - Find all restaurants.
    @Query(nativeQuery = true, value = """
        SELECT * FROM restaurants
    """)
    List<Restaurant> findAllRestaurants();


    // READ - Find restaurant by ID. Named binding (:restaurantId) because the argument uses @Param.
    @Query(nativeQuery = true, value = """
        SELECT * FROM restaurants WHERE id = :restaurantId
    """)
    Optional<Restaurant> findRestaurantById(@Param("restaurantId") int restaurantId);


    // UPDATE - Update a restaurant by ID.
    // Positional binding: ?1 = restaurantId, ?2 = name, ?3 = priceRange, ?4 = phone.
    // user_id, address_id and email are intentionally NOT updated.
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        UPDATE restaurants
        SET name = ?2, price_range = ?3, phone = ?4, update_on = NOW()
        WHERE id = ?1
    """)
    void updateRestaurant(int restaurantId, String name, int priceRange, String phone);


    // DELETE - Delete a restaurant by ID. Named binding (:restaurantId) because the argument uses @Param.
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        DELETE FROM restaurants WHERE id = :restaurantId
    """)
    void deleteRestaurantById(@Param("restaurantId") int restaurantId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        SELECT LAST_INSERT_ID() AS id
    """)
    int getLastInsertedId();
}
