package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// JPA
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Courier;
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.ProductOrder;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiAssignCourierDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiUpdateRatingDTO;
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiProductForOrderApiDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;

@Service       
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourierRepository courierRepository;

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
    // todo: Implement service methods that use DTOs for input/output.


    // CREATE - Create an address from DTO
    // todo: Implement service method to create an address from DTO, return created DTO with ID
    

    // READ - Get all addresses as DTOs
    // todo: Implement service method to get all addresses as DTOs


    // READ - Get an address by ID as DTO
    // todo: Implement service method to get an address by ID as DTO, return Optional.empty() if not found

    
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


    // UPDATE - Update an address from DTO
    // todo: Implement service method to update an address from DTO, return updated DTO, or Optional.empty() if not found


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


    // DELETE - Delete an address by ID, return true if found
    // todo: Implement service method to delete an address by ID, return true if found and deleted, false if not found

    
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
