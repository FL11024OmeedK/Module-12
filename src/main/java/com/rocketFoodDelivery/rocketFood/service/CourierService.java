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
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Courier;
import com.rocketFoodDelivery.rocketFood.models.CourierStatus;
import com.rocketFoodDelivery.rocketFood.models.User;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.courier.ApiCourierDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;

@Service       
public class CourierService {

    @Autowired
    private CourierRepository courierRepository;

    // Constructor
    public CourierService(CourierRepository courierRepository){
        this.courierRepository = courierRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Courier saveCourier(Courier courier) {
        return courierRepository.save(courier);
    }

    // READ - Find all couriers using JPA
    public List<Courier> findAllCouriers() {
        return courierRepository.findAll();
    }

    // READ - Find a courier by ID using JPA
    public Optional<Courier> findCourierById(int id) {
        return courierRepository.findById(id);
    }

    // READ - Find a courier by user ID using native SQL
    public Optional<Courier> findCourierByUserId(int userId) {
        return courierRepository.findCourierByUserId(userId);
    }

    // DELETE - Delete a courier by ID using JPA
    public void deleteCourierById(int id) {
        courierRepository.deleteById(id);
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


    // HELPER - Method to map Courier entity to DTO
    private ApiCourierDTO mapCourierToDTO(Courier courier) {
        ApiCourierDTO dto = new ApiCourierDTO();
        dto.setId(courier.getId());
        dto.setUserId(courier.getUser() != null ? courier.getUser().getId() : 0);
        dto.setAddressId(courier.getAddress() != null ? courier.getAddress().getId() : 0);
        dto.setCourierStatusId(courier.getCourierStatus() != null ? courier.getCourierStatus().getId() : 0);
        dto.setPhone(courier.getPhone());
        dto.setEmail(courier.getEmail());
        dto.setActive(courier.getActive());
        return dto;
    }
}
