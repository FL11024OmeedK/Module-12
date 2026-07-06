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
import com.rocketFoodDelivery.rocketFood.models.Employee;
import com.rocketFoodDelivery.rocketFood.models.User;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.employee.ApiEmployeeDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.EmployeeRepository;

@Service       
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Constructor
    public EmployeeService(EmployeeRepository employeeRepository){
        this.employeeRepository = employeeRepository;
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

    // READ - Find an employee by user ID
    public Optional<Employee> findEmployeeByUserId(int userId) {
        return employeeRepository.findEmployeeByUserId(userId);
    }

    // DELETE - Delete an employee by ID using JPA
    public void deleteEmployeeById(int id) {
        employeeRepository.deleteById(id);
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
