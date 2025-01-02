package com.example.CRUDApp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.CRUDApp.entity.Employee;
import com.example.CRUDApp.exception.EmailAlreadyExistsException;
import com.example.CRUDApp.exception.InvalidDataException;
import com.example.CRUDApp.exception.ResourceNotFoundException;
import com.example.CRUDApp.repository.EmployeeRepository;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // Create
    public Employee createEmployee(Employee employee) {
        // Validate employee data
        validateEmployee(employee);
        
        // Check if email already exists
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                "Employee with email " + employee.getEmail() + " already exists"
            );
        }
        
        return employeeRepository.save(employee);
    }
    
    // Read all
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    // Read one
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
    }
    
    // Update
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        // Validate employee data
        validateEmployee(employeeDetails);
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
        
        // Check if new email already exists for another employee
        Optional<Employee> existingEmployeeWithEmail = employeeRepository.findByEmail(employeeDetails.getEmail());
        if (existingEmployeeWithEmail.isPresent() && !existingEmployeeWithEmail.get().getId().equals(id)) {
            throw new EmailAlreadyExistsException(
                "Employee with email " + employeeDetails.getEmail() + " already exists"
            );
        }
        
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setSalary(employeeDetails.getSalary());
        
        return employeeRepository.save(employee);
    }
    
    // Delete
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Employee not found with id: " + id
            ));
        employeeRepository.delete(employee);
    }
    
    // Validate employee data
    private void validateEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new InvalidDataException("Employee name cannot be empty");
        }
        if (employee.getEmail() == null || !employee.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidDataException("Invalid email format");
        }
        if (employee.getSalary() == null || employee.getSalary() < 0) {
            throw new InvalidDataException("Salary must be a positive number");
        }
    }
}