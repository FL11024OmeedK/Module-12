package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Product;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiCreateProductDTO;
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiProductDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;

    // Constructor injection
    public ProductService(ProductRepository productRepository, RestaurantRepository restaurantRepository) {
        this.productRepository = productRepository;
        this.restaurantRepository = restaurantRepository;
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


    // CREATE - Insert a new product from a DTO and return it with its generated id.
    // @Transactional keeps saveProduct() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiProductDTO createProduct(ApiCreateProductDTO dto) {
        if (restaurantRepository.findById(dto.getRestaurantId()).isEmpty()) {
            throw new BadRequestException("Restaurant with id " + dto.getRestaurantId() + " not found");
        }

        productRepository.saveProduct(dto.getRestaurantId(), dto.getName(), dto.getDescription(), dto.getCost());

        int newId = productRepository.getLastInsertedId();
        return productRepository.findProductById(newId)
                .map(this::mapProductToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create product"));
    }


    // READ - Return every product as a DTO.
    public List<ApiProductDTO> getAllProductsAsDtos() {
        return productRepository.findAllProducts().stream()
                .map(this::mapProductToDTO)
                .toList();
    }


    // READ - Return a restaurant's products as DTOs (404 if the restaurant does not exist).
    public List<ApiProductDTO> getProductsByRestaurantAsDtos(int restaurantId) {
        if (restaurantRepository.findById(restaurantId).isEmpty()) {
            throw new ResourceNotFoundException("Restaurant with id " + restaurantId + " not found");
        }
        return productRepository.findProductsByRestaurantId(restaurantId).stream()
                .map(this::mapProductToDTO)
                .toList();
    }


    // READ - Return a single product as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiProductDTO> getProductByIdAsDto(int id) {
        return productRepository.findProductById(id)
                .map(this::mapProductToDTO);
    }


    // UPDATE - Update an existing product from a DTO (name, description, cost only).
    // Returns the updated DTO, or Optional.empty() if no product has the given id.
    @Transactional
    public Optional<ApiProductDTO> updateProduct(int id, ApiCreateProductDTO dto) {
        Optional<Product> existingOpt = productRepository.findProductById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        // restaurant_id is immutable on update — carry the stored value into the response.
        int restaurantId = existingOpt.get().getRestaurant() != null ? existingOpt.get().getRestaurant().getId() : 0;

        productRepository.updateProduct(id, dto.getName(), dto.getDescription(), dto.getCost());

        ApiProductDTO result = new ApiProductDTO();
        result.setId(id);
        result.setRestaurantId(restaurantId);
        result.setName(dto.getName());
        result.setDescription(dto.getDescription());
        result.setCost(dto.getCost());
        return Optional.of(result);
    }


    // DELETE - Delete a product by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteProduct(int id) {
        if (productRepository.findProductById(id).isEmpty()) {
            return false;
        }
        productRepository.deleteProductById(id);
        return true;
    }


    // HELPER - Method to map Product entity to DTO
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
