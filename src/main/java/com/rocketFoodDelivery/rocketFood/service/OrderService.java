package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// JPA
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Courier;
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.models.ProductOrder;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiAssignCourierDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiCreateOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiUpdateOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiUpdateRatingDTO;
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiProductForOrderApiDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderStatusRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductOrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Constructor
    public OrderService(OrderRepository orderRepository, CourierRepository courierRepository){
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // READ - Find all orders using JPA
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // READ - Find an order by ID using JPA
    public Optional<Order> findOrderById(int id) {
        return orderRepository.findById(id);
    }

    // READ - Find orders by restaurant ID using native SQL
    public List<Order> findOrdersByRestaurantId(int restaurantId) {
        return orderRepository.findOrdersByRestaurantId(restaurantId);
    }

    // READ - Find orders by customer ID using native SQL
    public List<Order> findOrdersByCustomerId(int customerId) {
        return orderRepository.findOrdersByCustomerId(customerId);
    }

    // READ - Find orders by courier ID using native SQL
    public List<Order> findOrdersByCourierId(int courierId) {
        return orderRepository.findOrdersByCourierId(courierId);
    }

    // UPDATE - Update only order status using native SQL
    @Transactional
    public void updateOrderStatus(int id, int orderStatusId) {
        orderRepository.updateOrderStatus(id, orderStatusId);
    }

    // DELETE - Delete an order by ID using JPA
    public void deleteOrderById(int id) {
        orderRepository.deleteById(id);
    }

    // ==================== DTO-Based Service Methods (used by API controller) ====================


    // CREATE - Create an order (with its product line items) from a DTO.
    // @Transactional keeps saveOrder(), getLastInsertedId(), and the line-item inserts on the same connection.
    @Transactional
    public ApiOrderDTO createOrder(ApiCreateOrderDTO dto) {
        if (dto.getProducts() == null || dto.getProducts().isEmpty()) {
            throw new BadRequestException("Products are required");
        }

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new BadRequestException("Restaurant with id " + dto.getRestaurantId() + " not found"));
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new BadRequestException("Customer with id " + dto.getCustomerId() + " not found"));

        // Validate each product: exists, belongs to the restaurant, and is not duplicated.
        Set<Integer> seenProductIds = new HashSet<>();
        List<Product> products = new ArrayList<>();
        for (ApiCreateOrderDTO.ProductItem item : dto.getProducts()) {
            if (!seenProductIds.add(item.getId())) {
                throw new BadRequestException("Cannot add the same product twice: product id " + item.getId());
            }
            if (item.getQuantity() < 1) {
                throw new BadRequestException("Product quantity must be at least 1");
            }
            Product product = productRepository.findById(item.getId())
                    .orElseThrow(() -> new BadRequestException("Product with id " + item.getId() + " not found"));
            if (product.getRestaurant() == null || product.getRestaurant().getId() != restaurant.getId()) {
                throw new BadRequestException("Product with id " + item.getId() + " does not belong to restaurant " + restaurant.getId());
            }
            products.add(product);
        }

        // New orders start in the "pending" status.
        OrderStatus pending = orderStatusRepository.findAll().stream()
                .filter(status -> "pending".equalsIgnoreCase(status.getName()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Pending order status not found"));

        orderRepository.saveOrder(restaurant.getId(), customer.getId(), pending.getId());
        int newOrderId = orderRepository.getLastInsertedId();

        // Persist one product_order line item per requested product (unit cost = product cost).
        Order orderRef = entityManager.getReference(Order.class, newOrderId);
        for (int i = 0; i < dto.getProducts().size(); i++) {
            ApiCreateOrderDTO.ProductItem item = dto.getProducts().get(i);
            Product product = products.get(i);
            ProductOrder productOrder = ProductOrder.builder()
                    .order(orderRef)
                    .product(product)
                    .productQuantity(item.getQuantity())
                    .productUnitCost(product.getCost())
                    .build();
            productOrderRepository.save(productOrder);
        }
        entityManager.flush();
        entityManager.clear();

        return orderRepository.findOrderById(newOrderId)
                .map(this::mapOrderToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create order"));
    }


    // READ - Get an order by ID as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiOrderDTO> getOrderByIdAsDto(int id) {
        return orderRepository.findOrderById(id).map(this::mapOrderToDTO);
    }


    // READ - Get orders by type (customer/restaurant/courier) and ID as DTOs
    public List<ApiOrderDTO> getOrdersByTypeAndId(String type, int id) {
        List<Order> orders;
        switch (type) {
            case "customer":
                orders = this.findOrdersByCustomerId(id);
                break;
            case "courier":
                orders = this.findOrdersByCourierId(id);
                break;
            default:
                orders = this.findOrdersByRestaurantId(id);
        }
        List<ApiOrderDTO> dtos = new ArrayList<>();
        for (Order order : orders) {
            dtos.add(mapOrderToDTO(order));
        }
        return dtos;
    }


    // UPDATE - Update an order from a DTO (reassign customer, restaurant, and optional courier).
    // Returns the updated DTO, or Optional.empty() if no order has the given id.
    @Transactional
    public Optional<ApiOrderDTO> updateOrder(int id, ApiUpdateOrderDTO dto) {
        Optional<Order> existing = orderRepository.findById(id);
        if (existing.isEmpty()) return Optional.empty();
        Order order = existing.get();

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new BadRequestException("Restaurant with id " + dto.getRestaurantId() + " not found"));
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new BadRequestException("Customer with id " + dto.getCustomerId() + " not found"));
        order.setRestaurant(restaurant);
        order.setCustomer(customer);

        if (dto.getCourierId() != null) {
            Courier courier = courierRepository.findById(dto.getCourierId())
                    .orElseThrow(() -> new BadRequestException("Courier with id " + dto.getCourierId() + " not found"));
            order.setCourier(courier);
        }

        this.saveOrder(order);
        entityManager.flush();
        entityManager.clear();
        return orderRepository.findOrderById(id).map(this::mapOrderToDTO);
    }


    // UPDATE - Assign a courier to an order
    @Transactional
    public Optional<ApiOrderDTO> assignCourier(int orderId, ApiAssignCourierDTO dto) {
        Optional<Order> existing = this.findOrderById(orderId);
        if (existing.isEmpty()) return Optional.empty();
        Optional<Courier> courier = courierRepository.findById(dto.getCourierId());
        if (courier.isEmpty()) return Optional.empty();
        Order order = existing.get();
        order.setCourier(courier.get());
        this.saveOrder(order);
        entityManager.flush();
        entityManager.clear();
        Optional<Order> updated = this.findOrderById(orderId);
        return updated.map(this::mapOrderToDTO);
    }

    // UPDATE - Update restaurant rating on an order
    @Transactional
    public Optional<ApiOrderDTO> updateRating(int orderId, ApiUpdateRatingDTO dto) {
        Optional<Order> existing = this.findOrderById(orderId);
        if (existing.isEmpty()) return Optional.empty();
        Order order = existing.get();
        order.setRestaurantRating(dto.getRestaurantRating());
        this.saveOrder(order);
        entityManager.flush();
        entityManager.clear();
        Optional<Order> updated = this.findOrderById(orderId);
        return updated.map(this::mapOrderToDTO);
    }


    // DELETE - Delete an order by id. Returns true if it existed and was deleted, false otherwise.
    // JPA deleteById cascades to the order's product_orders (cascade = ALL, orphanRemoval = true).
    @Transactional
    public boolean deleteOrder(int id) {
        if (orderRepository.findById(id).isEmpty()) {
            return false;
        }
        orderRepository.deleteById(id);
        return true;
    }


    // HELPER - Method to map Address entity to DTO
    private ApiOrderDTO mapOrderToDTO(Order order) {
        ApiOrderDTO dto = new ApiOrderDTO();
        dto.setId(order.getId());

        if (order.getCustomer() != null) {
            dto.setCustomer_id(order.getCustomer().getId());
            if (order.getCustomer().getUser() != null) {
                dto.setCustomer_name(order.getCustomer().getUser().getName());
            }
            if (order.getCustomer().getAddress() != null) {
                dto.setCustomer_address(order.getCustomer().getAddress().getStreetAddress());
            }
        }

        if (order.getRestaurant() != null) {
            dto.setRestaurant_id(order.getRestaurant().getId());
            dto.setRestaurant_name(order.getRestaurant().getName());
            if (order.getRestaurant().getAddress() != null) {
                dto.setRestaurant_address(order.getRestaurant().getAddress().getStreetAddress());
            }
        }

        if (order.getOrderStatus() != null) {
            dto.setStatus(order.getOrderStatus().getName());
        }

        if (order.getCourier() != null) {
            dto.setCourier_id(order.getCourier().getId());
            if (order.getCourier().getUser() != null) {
                dto.setCourier_name(order.getCourier().getUser().getName());
            }
        }

        List<ApiProductForOrderApiDTO> productDtos = new ArrayList<>();
        long totalCost = 0;
        if (order.getProductOrders() != null) {
            for (ProductOrder po : order.getProductOrders()) {
                ApiProductForOrderApiDTO pDto = new ApiProductForOrderApiDTO();
                pDto.setId(po.getProduct().getId());
                pDto.setProduct_name(po.getProduct().getName());
                pDto.setQuantity(po.getProductQuantity());
                pDto.setUnit_cost(po.getProductUnitCost());
                pDto.setTotal_cost(po.getProductQuantity() * po.getProductUnitCost());
                productDtos.add(pDto);
                totalCost += (long) po.getProductQuantity() * po.getProductUnitCost();
            }
        }
        dto.setProducts(productDtos);
        dto.setTotal_cost(totalCost);
        dto.setCreated_on(order.getCreatedOn());

        return dto;
    }
}
