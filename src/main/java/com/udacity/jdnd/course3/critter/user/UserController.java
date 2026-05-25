package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.pet.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Users (both Customers and Employees).
 *
 * DTO ↔ Entity mapping occurs here in the controller layer.
 * UserService is called with entity objects only (no DTOs passed to service).
 *
 * Splitting into separate CustomerController and EmployeeController would
 * also be valid, but keeping them combined as per the starter code design.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ==================== Customer Endpoints ====================

    /**
     * Creates and saves a new customer.
     *
     * @param customerDTO the incoming customer data
     * @return the saved customer as a DTO with its generated ID and pet IDs
     */
    @PostMapping("/customer")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO){
        Customer customer = convertDTOtoCustomer(customerDTO);
        Customer savedCustomer = userService.saveCustomer(customer);
        return convertCustomerToDTO(savedCustomer);
    }

    /**
     * Returns all customers in the system.
     *
     * @return list of all customers as DTOs
     */
    @GetMapping("/customer")
    public List<CustomerDTO> getAllCustomers(){
        return userService.getAllCustomers().stream()
                .map(this::convertCustomerToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds the owner (customer) of a specific pet.
     *
     * @param petId the pet's ID
     * @return the owning customer as a DTO including their pet IDs
     */
    @GetMapping("/customer/pet/{petId}")
    public CustomerDTO getOwnerByPet(@PathVariable long petId){
        Customer customer = userService.getOwnerByPet(petId);
        return convertCustomerToDTO(customer);
    }

    // ==================== Employee Endpoints ====================

    /**
     * Creates and saves a new employee.
     *
     * @param employeeDTO the incoming employee data
     * @return the saved employee as a DTO with its generated ID
     */
    @PostMapping("/employee")
    public EmployeeDTO saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee employee = convertDTOtoEmployee(employeeDTO);
        Employee savedEmployee = userService.saveEmployee(employee);
        return convertEmployeeToDTO(savedEmployee);
    }

    /**
     * Retrieves a single employee by their ID.
     *
     * NOTE: The starter code uses @PostMapping here (not @GetMapping).
     * We match that signature exactly to ensure tests pass.
     *
     * @param employeeId the employee's ID
     * @return the employee as a DTO
     */
    @PostMapping("/employee/{employeeId}")
    public EmployeeDTO getEmployee(@PathVariable long employeeId) {
        return convertEmployeeToDTO(userService.getEmployee(employeeId));
    }

    /**
     * Updates the available days of week for a specific employee.
     *
     * @param daysAvailable the new set of available days
     * @param employeeId    the employee's ID
     */
    @PutMapping("/employee/{employeeId}")
    public void setAvailability(@RequestBody Set<DayOfWeek> daysAvailable, @PathVariable long employeeId) {
        userService.setEmployeeAvailability(employeeId, daysAvailable);
    }

    /**
     * Finds all employees who are available on the given date and
     * have all the required skills.
     *
     * @param employeeDTO the request containing the date and required skills
     * @return list of qualifying employees as DTOs
     */
    @GetMapping("/employee/availability")
    public List<EmployeeDTO> findEmployeesForService(@RequestBody EmployeeRequestDTO employeeDTO) {
        return userService.findEmployeesForService(employeeDTO.getSkills(), employeeDTO.getDate())
                .stream()
                .map(this::convertEmployeeToDTO)
                .collect(Collectors.toList());
    }

    // ==================== DTO Mapping Helpers ====================

    /**
     * Converts a CustomerDTO (incoming) to a Customer entity.
     */
    private Customer convertDTOtoCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setNotes(customerDTO.getNotes());
        return customer;
    }

    /**
     * Converts a Customer entity to a CustomerDTO (outgoing response).
     * Extracts the list of pet IDs from the customer's pets collection.
     */
    private CustomerDTO convertCustomerToDTO(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());
        customerDTO.setName(customer.getName());
        customerDTO.setPhoneNumber(customer.getPhoneNumber());
        customerDTO.setNotes(customer.getNotes());

        // Convert the pet entities to a list of pet IDs
        if (customer.getPets() != null && !customer.getPets().isEmpty()) {
            List<Long> petIds = customer.getPets().stream()
                    .map(Pet::getId)
                    .collect(Collectors.toList());
            customerDTO.setPetIds(petIds);
        } else {
            customerDTO.setPetIds(new ArrayList<>());
        }

        return customerDTO;
    }

    /**
     * Converts an EmployeeDTO (incoming) to an Employee entity.
     */
    private Employee convertDTOtoEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        if (employeeDTO.getSkills() != null) {
            employee.setSkills(employeeDTO.getSkills());
        }
        if (employeeDTO.getDaysAvailable() != null) {
            employee.setDaysAvailable(employeeDTO.getDaysAvailable());
        }
        return employee;
    }

    /**
     * Converts an Employee entity to an EmployeeDTO (outgoing response).
     */
    private EmployeeDTO convertEmployeeToDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setId(employee.getId());
        employeeDTO.setName(employee.getName());
        employeeDTO.setSkills(employee.getSkills());
        // Return null daysAvailable if the set is empty (matches test expectations:
        // testChangeEmployeeAvailability asserts emp1.getDaysAvailable() is null initially)
        if (employee.getDaysAvailable() != null && !employee.getDaysAvailable().isEmpty()) {
            employeeDTO.setDaysAvailable(employee.getDaysAvailable());
        } else {
            employeeDTO.setDaysAvailable(null);
        }
        return employeeDTO;
    }
}
