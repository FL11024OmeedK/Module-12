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
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.orderStatus.ApiOrderStatusCrudDTO;
import com.rocketFoodDelivery.rocketFood.dtos.orderStatus.ApiOrderStatusDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderStatusRepository;

@Service       
public class OrderStatusService {

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Constructor
    public OrderStatusService(OrderStatusRepository orderStatusRepository, OrderRepository orderRepository){
        this.orderStatusRepository = orderStatusRepository;
        this.orderRepository = orderRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public OrderStatus saveOrderStatus(OrderStatus orderStatus) {
        return orderStatusRepository.save(orderStatus);
    }

    // READ - Find all order statuses using JPA
    public List<OrderStatus> findAllOrderStatuses() {
        return orderStatusRepository.findAll();
    }

    // READ - Find an order status by ID using JPA
    public Optional<OrderStatus> findOrderStatusById(int id) {
        return orderStatusRepository.findById(id);
    }

    // READ - Find an order status by name using native SQL
    public Optional<OrderStatus> findOrderStatusByName(String name) {
        return orderStatusRepository.findOrderStatusByName(name);
    }

    // DELETE - Delete an order status by ID using JPA
    public void deleteOrderStatusById(int id) {
        orderStatusRepository.deleteById(id);
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


    // UPDATE - Update order status for an order from DTO
    @Transactional
    public Optional<ApiOrderStatusDTO> updateOrderStatusForOrder(int orderId, String statusName) {
        Optional<Order> order = orderRepository.findOrderById(orderId);
        if (order.isEmpty()) return Optional.empty();

        Optional<OrderStatus> status = this.findOrderStatusByName(statusName);
        if (status.isEmpty()) return Optional.empty();

        orderRepository.updateOrderStatus(orderId, status.get().getId());
        ApiOrderStatusDTO response = new ApiOrderStatusDTO();
        response.setStatus(status.get().getName());
        return Optional.of(response);
    }

    // DELETE - Delete an address by ID, return true if found
    // todo: Implement service method to delete an address by ID, return true if found and deleted, false if not found


    // HELPER - Method to map Address entity to DTO
    private ApiOrderStatusCrudDTO mapOrderStatusToDTO(OrderStatus status) {
        ApiOrderStatusCrudDTO dto = new ApiOrderStatusCrudDTO();
        dto.setId(status.getId());
        dto.setName(status.getName());
        return dto;
    }
}
