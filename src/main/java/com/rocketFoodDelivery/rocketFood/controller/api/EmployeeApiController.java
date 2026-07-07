package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.employee.ApiEmployeeDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.EmployeeService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeApiController {

    // Service dependency (constructor injection)
    private final EmployeeService employeeService;

    public EmployeeApiController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/employees - Get all employees
    @GetMapping("/api/employees")
    public ResponseEntity<Object> getAllEmployees() {
        return ResponseBuilder.buildOkResponse(employeeService.getAllEmployeesAsDtos());
    }


    // GET /api/employees/{id} - Get employee by ID
    @GetMapping("/api/employees/{id}")
    public ResponseEntity<Object> getEmployeeById(@PathVariable int id) {
        ApiEmployeeDTO employee = employeeService.getEmployeeByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(employee);
    }


    // POST /api/employees - Create new employee
    @PostMapping("/api/employees")
    public ResponseEntity<Object> createEmployee(@Valid @RequestBody ApiEmployeeDTO employeeDto) {
        ApiEmployeeDTO created = employeeService.createEmployee(employeeDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/employees/{id} - Update employee by ID
    @PutMapping("/api/employees/{id}")
    public ResponseEntity<Object> updateEmployee(@PathVariable int id, @Valid @RequestBody ApiEmployeeDTO employeeDto) {
        ApiEmployeeDTO updated = employeeService.updateEmployee(id, employeeDto)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/employees/{id} - Delete employee by ID
    @DeleteMapping("/api/employees/{id}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiEmployeeDTO employee = employeeService.getEmployeeByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + id + " not found"));
        employeeService.deleteEmployee(id);
        return ResponseBuilder.buildOkResponse(employee);
    }
}
