package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiCreateProductDTO;
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiProductDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;

@Service       
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Constructor
    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // READ - Find all products using JPA
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // READ - Find a product by ID using JPA
    public Optional<Product> findProductById(int id) {
        return productRepository.findById(id);
    }

    // READ - Find products by restaurant ID using native SQL
    public List<Product> findProductsByRestaurantId(int restaurantId) {
        return productRepository.findProductsByRestaurantId(restaurantId);
    }

    // DELETE - Delete a product by ID using JPA
    public void deleteProductById(int id) {
        productRepository.deleteById(id);
    }

    // ==================== DTO-Based Service Methods (used by API controller) ====================
    // todo: Implement service methods that use DTOs for input/output.


    // CREATE - Create an address from DTO
    // todo: Implement service method to create an address from DTO, return created DTO with ID
    

    // READ - Get all addresses as DTOs
    // todo: Implement service method to get all addresses as DTOs


    // READ - Get an address by ID as DTO
    // todo: Implement service method to get an address by ID as DTO, return Optional.empty() if not found


    // UPDATE - Update an address from DTO
    // todo: Implement service method to update an address from DTO, return updated DTO, or Optional.empty() if not found


    // DELETE - Delete an address by ID, return true if found
    // todo: Implement service method to delete an address by ID, return true if found and deleted, false if not found


    // HELPER - Method to map Address entity to DTO
    private ApiProductDTO mapProductToDTO(Product product) {
        ApiProductDTO dto = new ApiProductDTO();
        dto.setId(product.getId());
        dto.setRestaurantId(product.getRestaurant() != null ? product.getRestaurant().getId() : 0);
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCost(product.getCost());
        return dto;
    }
}
