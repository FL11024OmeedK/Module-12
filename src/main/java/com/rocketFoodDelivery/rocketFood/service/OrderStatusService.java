package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
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

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

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


    // CREATE - Insert a new order status from a DTO and return it with its generated id.
    // @Transactional keeps saveOrderStatus() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiOrderStatusCrudDTO createOrderStatus(ApiOrderStatusCrudDTO dto) {
        orderStatusRepository.saveOrderStatus(dto.getName());

        int newId = orderStatusRepository.getLastInsertedId();
        return orderStatusRepository.findOrderStatusById(newId)
                .map(this::mapOrderStatusToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create order status"));
    }


    // READ - Return every order status as a DTO.
    public List<ApiOrderStatusCrudDTO> getAllOrderStatusesAsDtos() {
        return orderStatusRepository.findAllOrderStatuses().stream()
                .map(this::mapOrderStatusToDTO)
                .toList();
    }


    // READ - Return a single order status as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiOrderStatusCrudDTO> getOrderStatusByIdAsDto(int id) {
        return orderStatusRepository.findOrderStatusById(id)
                .map(this::mapOrderStatusToDTO);
    }


    // UPDATE - Update an existing order status from a DTO.
    // Returns the updated DTO, or Optional.empty() if no status has the given id.
    @Transactional
    public Optional<ApiOrderStatusCrudDTO> updateOrderStatus(int id, ApiOrderStatusCrudDTO dto) {
        if (orderStatusRepository.findOrderStatusById(id).isEmpty()) {
            return Optional.empty();
        }

        orderStatusRepository.updateOrderStatus(id, dto.getName());

        dto.setId(id);
        return Optional.of(dto);
    }


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


    // DELETE - Delete an order status by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteOrderStatus(int id) {
        if (orderStatusRepository.findOrderStatusById(id).isEmpty()) {
            return false;
        }
        orderStatusRepository.deleteOrderStatusById(id);
        return true;
    }


    // HELPER - Method to map OrderStatus entity to DTO
    private ApiOrderStatusCrudDTO mapOrderStatusToDTO(OrderStatus status) {
        ApiOrderStatusCrudDTO dto = new ApiOrderStatusCrudDTO();
        dto.setId(status.getId());
        dto.setName(status.getName());
        return dto;
    }
}
