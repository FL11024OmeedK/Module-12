package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Courier;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.courier.ApiCourierDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CourierStatusRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CourierStatusRepository courierStatusRepository;

    // Constructor injection
    public CourierService(CourierRepository courierRepository,
                          UserRepository userRepository,
                          AddressRepository addressRepository,
                          CourierStatusRepository courierStatusRepository) {
        this.courierRepository = courierRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.courierStatusRepository = courierStatusRepository;
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


    // CREATE - Create a courier from a DTO and return it with its generated id.
    // @Transactional keeps saveCourier() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiCourierDTO createCourier(ApiCourierDTO dto) {
        validateReferences(dto);

        // One courier per user (also enforced by the DB unique constraint on user_id).
        if (courierRepository.findCourierByUserId(dto.getUserId()).isPresent()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " already has a courier");
        }

        courierRepository.saveCourier(
                dto.getUserId(),
                dto.getAddressId(),
                dto.getCourierStatusId(),
                dto.getPhone(),
                dto.getEmail());

        int newId = courierRepository.getLastInsertedId();
        return courierRepository.findCourierById(newId)
                .map(this::mapCourierToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create courier"));
    }


    // READ - Return every courier as a DTO.
    public List<ApiCourierDTO> getAllCouriersAsDtos() {
        return courierRepository.findAllCouriers().stream()
                .map(this::mapCourierToDTO)
                .toList();
    }


    // READ - Return a single courier as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiCourierDTO> getCourierByIdAsDto(int id) {
        return courierRepository.findCourierById(id)
                .map(this::mapCourierToDTO);
    }


    // UPDATE - Update an existing courier from a DTO (status, phone, email, active only).
    // Returns the updated DTO, or Optional.empty() if no courier has the given id.
    @Transactional
    public Optional<ApiCourierDTO> updateCourier(int id, ApiCourierDTO dto) {
        Optional<Courier> existingOpt = courierRepository.findCourierById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        Courier existing = existingOpt.get();
        // user_id and address_id are immutable on update — carry the stored values into the response.
        int userId = existing.getUser().getId();
        int addressId = existing.getAddress().getId();

        boolean active = dto.getActive() != null ? dto.getActive() : true;
        courierRepository.updateCourier(id, dto.getCourierStatusId(), dto.getPhone(), dto.getEmail(), active);

        ApiCourierDTO result = new ApiCourierDTO();
        result.setId(id);
        result.setUserId(userId);
        result.setAddressId(addressId);
        result.setCourierStatusId(dto.getCourierStatusId());
        result.setPhone(dto.getPhone());
        result.setEmail(dto.getEmail());
        result.setActive(active);
        return Optional.of(result);
    }


    // DELETE - Delete a courier by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteCourier(int id) {
        if (courierRepository.findCourierById(id).isEmpty()) {
            return false;
        }
        courierRepository.deleteCourierById(id);
        return true;
    }


    // HELPER - Validate that the referenced user, address, and courier status all exist.
    private void validateReferences(ApiCourierDTO dto) {
        if (userRepository.findById(dto.getUserId()).isEmpty()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " not found");
        }
        if (addressRepository.findById(dto.getAddressId()).isEmpty()) {
            throw new BadRequestException("Address with id " + dto.getAddressId() + " not found");
        }
        if (courierStatusRepository.findById(dto.getCourierStatusId()).isEmpty()) {
            throw new BadRequestException("Courier status with id " + dto.getCourierStatusId() + " not found");
        }
    }


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
