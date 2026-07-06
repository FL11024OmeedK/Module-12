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
import com.rocketFoodDelivery.rocketFood.models.CourierStatus;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.courierStatus.ApiCourierStatusDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.CourierStatusRepository;

@Service       
public class CourierStatusService {

    @Autowired
    private CourierStatusRepository courierStatusRepository;

    // Constructor
    public CourierStatusService(CourierStatusRepository courierStatusRepository){
        this.courierStatusRepository = courierStatusRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public CourierStatus saveCourierStatus(CourierStatus courierStatus) {
        return courierStatusRepository.save(courierStatus);
    }

    // READ - Find all courier statuses using JPA
    public List<CourierStatus> findAllCourierStatuses() {
        return courierStatusRepository.findAll();
    }

    // READ - Find a courier status by ID using JPA
    public Optional<CourierStatus> findCourierStatusById(int id) {
        return courierStatusRepository.findById(id);
    }

    // DELETE - Delete a courier status by ID using JPA
    public void deleteCourierStatusById(int id) {
        courierStatusRepository.deleteById(id);
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
    private ApiCourierStatusDTO mapCourierStatusToDTO(CourierStatus status) {
        ApiCourierStatusDTO dto = new ApiCourierStatusDTO();
        dto.setId(status.getId());
        dto.setName(status.getName());
        return dto;
    }
}
