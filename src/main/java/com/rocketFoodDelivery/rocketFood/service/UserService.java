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
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUpdateAccountDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUserDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;

// Project services

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