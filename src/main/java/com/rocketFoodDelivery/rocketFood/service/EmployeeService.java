package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Employee;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.employee.ApiEmployeeDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.EmployeeRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // Constructor injection
    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           AddressRepository addressRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    // READ - Find all employees using JPA
    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    // READ - Find an employee by ID using JPA
    public Optional<Employee> findEmployeeById(int id) {
        return employeeRepository.findById(id);
    }

    // READ - Find an employee by user ID using native SQL
    public Optional<Employee> findEmployeeByUserId(int userId) {
        return employeeRepository.findEmployeeByUserId(userId);
    }

    // DELETE - Delete an employee by ID using JPA
    public void deleteEmployeeById(int id) {
        employeeRepository.deleteById(id);
    }

    // ==================== DTO-Based Service Methods (used by API controller) ====================


    // CREATE - Create an employee from a DTO and return it with its generated id.
    // @Transactional keeps saveEmployee() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiEmployeeDTO createEmployee(ApiEmployeeDTO dto) {
        validateReferences(dto);

        // One employee per user (also enforced by the DB unique constraint on user_id).
        if (employeeRepository.findEmployeeByUserId(dto.getUserId()).isPresent()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " already has an employee");
        }

        employeeRepository.saveEmployee(
                dto.getUserId(),
                dto.getAddressId(),
                dto.getPhone(),
                dto.getEmail());

        int newId = employeeRepository.getLastInsertedId();
        return employeeRepository.findEmployeeById(newId)
                .map(this::mapEmployeeToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create employee"));
    }


    // READ - Return every employee as a DTO.
    public List<ApiEmployeeDTO> getAllEmployeesAsDtos() {
        return employeeRepository.findAllEmployees().stream()
                .map(this::mapEmployeeToDTO)
                .toList();
    }


    // READ - Return a single employee as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiEmployeeDTO> getEmployeeByIdAsDto(int id) {
        return employeeRepository.findEmployeeById(id)
                .map(this::mapEmployeeToDTO);
    }


    // UPDATE - Update an existing employee from a DTO (phone, email only).
    // Returns the updated DTO, or Optional.empty() if no employee has the given id.
    @Transactional
    public Optional<ApiEmployeeDTO> updateEmployee(int id, ApiEmployeeDTO dto) {
        Optional<Employee> existingOpt = employeeRepository.findEmployeeById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        Employee existing = existingOpt.get();
        // user_id and address_id are immutable on update — carry the stored values into the response.
        int userId = existing.getUser().getId();
        int addressId = existing.getAddress().getId();

        employeeRepository.updateEmployee(id, dto.getPhone(), dto.getEmail());

        ApiEmployeeDTO result = new ApiEmployeeDTO();
        result.setId(id);
        result.setUserId(userId);
        result.setAddressId(addressId);
        result.setPhone(dto.getPhone());
        result.setEmail(dto.getEmail());
        return Optional.of(result);
    }


    // DELETE - Delete an employee by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteEmployee(int id) {
        if (employeeRepository.findEmployeeById(id).isEmpty()) {
            return false;
        }
        employeeRepository.deleteEmployeeById(id);
        return true;
    }


    // HELPER - Validate that the referenced user and address exist.
    private void validateReferences(ApiEmployeeDTO dto) {
        if (userRepository.findById(dto.getUserId()).isEmpty()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " not found");
        }
        if (addressRepository.findById(dto.getAddressId()).isEmpty()) {
            throw new BadRequestException("Address with id " + dto.getAddressId() + " not found");
        }
    }


    // HELPER - Method to map Employee entity to DTO
    private ApiEmployeeDTO mapEmployeeToDTO(Employee employee) {
        ApiEmployeeDTO dto = new ApiEmployeeDTO();
        dto.setId(employee.getId());
        dto.setUserId(employee.getUser() != null ? employee.getUser().getId() : 0);
        dto.setAddressId(employee.getAddress() != null ? employee.getAddress().getId() : 0);
        dto.setPhone(employee.getPhone());
        dto.setEmail(employee.getEmail());
        return dto;
    }
}
