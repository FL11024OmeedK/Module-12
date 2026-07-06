package com.rocketFoodDelivery.rocketFood.repository;


// Java standard library


// Spring Framework
import com.rocketFoodDelivery.rocketFood.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


// Java standard library
import java.util.List;
import java.util.Optional;


// Repository interface that provides CRUD operations for database access
@Repository
public interface EmployeeRepository extends JpaRepository <Employee, Integer> {
    // Spring Data JPA automatically implements CRUD operations inherited from JpaRepository.
    // save(), findAll(), findById(), deleteById()


    // ==================== Native SQL CRUD Queries ====================
    // todo: Add Native SQL queries for CRUD operations on the employees table


    // CREATE - Insert a new employee
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to save an employee
    """)
    void saveEmployee(int userId, int addressId, String phone, String email);


    // READ - Find all employees
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find all employees
    """)
    List<Employee> findAllEmployees();


    // READ - Find employee by ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find an employee by ID
    """)
    Optional<Employee> findEmployeeById(@Param("employeeId") int employeeId);

    
    // READ - Find employee by user ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to find an employee by user ID
    """)
    Optional<Employee> findEmployeeByUserId(@Param("userId") int userId);


    // UPDATE - Update an employee by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to update an employee by ID
    """)
    void updateEmployee(int employeeId, String phone, String email);


    // DELETE - Delete an employee by ID
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to delete an employee by ID
    """)
    void deleteEmployeeById(@Param("employeeId") int employeeId);


    // GET - Get the last inserted ID
    @Query(nativeQuery = true, value = """
        // todo: Add Native SQL query to get the last inserted ID
    """)
    int getLastInsertedId();
}
