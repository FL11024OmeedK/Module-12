package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Courier;
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.Employee;
import com.rocketFoodDelivery.rocketFood.models.User;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiAccountDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiCreateUserDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUpdateAccountDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUserDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourierService courierService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmployeeService employeeService;

    // Constructor
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // READ - Find all users using JPA
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // READ - Find a user by ID using JPA
    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    // DELETE - Delete a user by ID using JPA
    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }

    // ==================== DTO-Based Service Methods (used by API controller) ====================


    // CREATE - Insert a new user from a DTO and return it with its generated id (never the password).
    // @Transactional keeps saveUser() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiUserDTO createUser(ApiCreateUserDTO dto) {
        // Email must be unique (also enforced by the DB unique constraint).
        if (userRepository.findUserByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("Email " + dto.getEmail() + " is already in use");
        }

        userRepository.saveUser(dto.getName(), dto.getEmail(), dto.getPassword());

        int newId = userRepository.getLastInsertedId();
        return userRepository.findUserById(newId)
                .map(this::mapUserToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create user"));
    }


    // READ - Return every user as a DTO.
    public List<ApiUserDTO> getAllUsersAsDtos() {
        return userRepository.findAllUsers().stream()
                .map(this::mapUserToDTO)
                .toList();
    }


    // READ - Return a single user as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiUserDTO> getUserByIdAsDto(int id) {
        return userRepository.findUserById(id)
                .map(this::mapUserToDTO);
    }


    // UPDATE - Update an existing user from a DTO (name, email, password).
    // Returns the updated DTO, or Optional.empty() if no user has the given id.
    @Transactional
    public Optional<ApiUserDTO> updateUser(int id, ApiCreateUserDTO dto) {
        if (userRepository.findUserById(id).isEmpty()) {
            return Optional.empty();
        }

        userRepository.updateUser(id, dto.getName(), dto.getEmail(), dto.getPassword());

        ApiUserDTO result = new ApiUserDTO();
        result.setId(id);
        result.setName(dto.getName());
        result.setEmail(dto.getEmail());
        return Optional.of(result);
    }


    // DELETE - Delete a user by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteUser(int id) {
        if (userRepository.findUserById(id).isEmpty()) {
            return false;
        }
        userRepository.deleteUserById(id);
        return true;
    }


    // HELPER - Method to map User entity to DTO (id, name, email only — never the password)
    private ApiUserDTO mapUserToDTO(User user) {
        ApiUserDTO dto = new ApiUserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }


    // READ - Get account details (including role-specific details) as DTO
    public Optional<ApiAccountDTO> getAccountDTO(int userId) {
        Optional<User> userOpt = this.findUserById(userId);
        if (userOpt.isEmpty()) return Optional.empty();
        User user = userOpt.get();

        ApiAccountDTO dto = new ApiAccountDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        customerService.findCustomerByUserId(userId).ifPresent(c -> {
            ApiAccountDTO.RoleDetail detail = new ApiAccountDTO.RoleDetail();
            detail.setId(c.getId());
            detail.setPhone(c.getPhone());
            detail.setEmail(c.getEmail());
            if (c.getAddress() != null) detail.setAddress(c.getAddress().getStreetAddress());
            dto.setCustomer(detail);
        });

        courierService.findCourierByUserId(userId).ifPresent(c -> {
            ApiAccountDTO.RoleDetail detail = new ApiAccountDTO.RoleDetail();
            detail.setId(c.getId());
            detail.setPhone(c.getPhone());
            detail.setEmail(c.getEmail());
            if (c.getAddress() != null) detail.setAddress(c.getAddress().getStreetAddress());
            dto.setCourier(detail);
        });

        employeeService.findEmployeeByUserId(userId).ifPresent(e -> {
            ApiAccountDTO.RoleDetail detail = new ApiAccountDTO.RoleDetail();
            detail.setId(e.getId());
            detail.setPhone(e.getPhone());
            detail.setEmail(e.getEmail());
            if (e.getAddress() != null) detail.setAddress(e.getAddress().getStreetAddress());
            dto.setEmployee(detail);
        });

        return Optional.of(dto);
    }


    // UPDATE - Update account details (phone, email) for a user based on role
    @Transactional
    public Optional<ApiAccountDTO> updateAccount(int userId, String type, ApiUpdateAccountDTO updateDTO) {
        Optional<User> userOpt = this.findUserById(userId);
        if (userOpt.isEmpty()) return Optional.empty();

        switch (type) {
            case "customer" -> {
                Optional<Customer> opt = customerService.findCustomerByUserId(userId);
                if (opt.isEmpty()) return Optional.empty();
                Customer c = opt.get();
                if (updateDTO.getEmail() != null) c.setEmail(updateDTO.getEmail());
                if (updateDTO.getPhone() != null) c.setPhone(updateDTO.getPhone());
                customerService.saveCustomer(c);
            }
            case "courier" -> {
                Optional<Courier> opt = courierService.findCourierByUserId(userId);
                if (opt.isEmpty()) return Optional.empty();
                Courier c = opt.get();
                if (updateDTO.getEmail() != null) c.setEmail(updateDTO.getEmail());
                if (updateDTO.getPhone() != null) c.setPhone(updateDTO.getPhone());
                courierService.saveCourier(c);
            }
            case "employee" -> {
                Optional<Employee> opt = employeeService.findEmployeeByUserId(userId);
                if (opt.isEmpty()) return Optional.empty();
                Employee e = opt.get();
                if (updateDTO.getEmail() != null) e.setEmail(updateDTO.getEmail());
                if (updateDTO.getPhone() != null) e.setPhone(updateDTO.getPhone());
                employeeService.saveEmployee(e);
            }
            default -> {
                return Optional.empty();
            }
        }

        return getAccountDTO(userId);
    }
}
