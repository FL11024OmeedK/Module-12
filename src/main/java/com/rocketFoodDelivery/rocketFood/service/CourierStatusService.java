package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.CourierStatus;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.courierStatus.ApiCourierStatusDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.CourierStatusRepository;

@Service
public class CourierStatusService {

    private final CourierStatusRepository courierStatusRepository;

    // Constructor injection
    public CourierStatusService(CourierStatusRepository courierStatusRepository) {
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


    // CREATE - Insert a new courier status from a DTO and return it with its generated id.
    // @Transactional keeps saveCourierStatus() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiCourierStatusDTO createCourierStatus(ApiCourierStatusDTO dto) {
        courierStatusRepository.saveCourierStatus(dto.getName());

        int newId = courierStatusRepository.getLastInsertedId();
        return courierStatusRepository.findCourierStatusById(newId)
                .map(this::mapCourierStatusToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create courier status"));
    }


    // READ - Return every courier status as a DTO.
    public List<ApiCourierStatusDTO> getAllCourierStatusesAsDtos() {
        return courierStatusRepository.findAllCourierStatuses().stream()
                .map(this::mapCourierStatusToDTO)
                .toList();
    }


    // READ - Return a single courier status as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiCourierStatusDTO> getCourierStatusByIdAsDto(int id) {
        return courierStatusRepository.findCourierStatusById(id)
                .map(this::mapCourierStatusToDTO);
    }


    // UPDATE - Update an existing courier status from a DTO.
    // Returns the updated DTO, or Optional.empty() if no status has the given id.
    @Transactional
    public Optional<ApiCourierStatusDTO> updateCourierStatus(int id, ApiCourierStatusDTO dto) {
        if (courierStatusRepository.findCourierStatusById(id).isEmpty()) {
            return Optional.empty();
        }

        courierStatusRepository.updateCourierStatus(id, dto.getName());

        dto.setId(id);
        return Optional.of(dto);
    }


    // DELETE - Delete a courier status by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteCourierStatus(int id) {
        if (courierStatusRepository.findCourierStatusById(id).isEmpty()) {
            return false;
        }
        courierStatusRepository.deleteCourierStatusById(id);
        return true;
    }


    // HELPER - Method to map CourierStatus entity to DTO
    private ApiCourierStatusDTO mapCourierStatusToDTO(CourierStatus status) {
        ApiCourierStatusDTO dto = new ApiCourierStatusDTO();
        dto.setId(status.getId());
        dto.setName(status.getName());
        return dto;
    }
}
