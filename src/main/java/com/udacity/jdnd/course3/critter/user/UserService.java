package com.udacity.jdnd.course3.critter.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for Customer and Employee operations.
 *
 * This layer handles business logic that spans multiple data-access calls
 * (e.g., finding available employees for a specific skill set and date).
 *
 * Transaction boundary: All methods are marked @Transactional so that DB
 * operations are atomic. Read-only methods use readOnly=true as a hint
 * to the JPA provider to optimize for non-mutating queries.
 *
 * This layer works exclusively with Customer/Employee entities — no DTOs.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // ==================== Customer Operations ====================

    /**
     * Saves a new customer.
     *
     * @param customer the customer entity (populated from DTO in controller)
     * @return the persisted customer with generated ID
     */
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    /**
     * Returns all customers in the system.
     */
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Finds the customer (owner) of a specific pet.
     *
     * @param petId the pet's ID
     * @return the customer who owns the pet
     * @throws IllegalArgumentException if no owner is found for the given pet
     */
    @Transactional(readOnly = true)
    public Customer getOwnerByPet(long petId) {
        Customer customer = customerRepository.findByPetsId(petId);
        if (customer == null) {
            throw new IllegalArgumentException("No owner found for pet id: " + petId);
        }
        return customer;
    }

    // ==================== Employee Operations ====================

    /**
     * Saves a new employee.
     *
     * @param employee the employee entity (populated from DTO in controller)
     * @return the persisted employee with generated ID
     */
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Retrieves a single employee by their ID.
     *
     * @param employeeId the employee's ID
     * @return the found Employee entity
     * @throws IllegalArgumentException if no employee exists with that ID
     */
    @Transactional(readOnly = true)
    public Employee getEmployee(long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No employee found with id: " + employeeId));
    }

    /**
     * Updates the availability (days of the week) for a given employee.
     *
     * Validation is done here in the service layer rather than in the
     * repository to keep data-layer logic focused on CRUD.
     *
     * @param employeeId    the ID of the employee to update
     * @param daysAvailable the new set of available days
     */
    public void setEmployeeAvailability(long employeeId, Set<DayOfWeek> daysAvailable) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No employee found with id: " + employeeId));
        employee.setDaysAvailable(daysAvailable);
        employeeRepository.save(employee);
    }

    /**
     * Finds all employees who:
     *  1. Are available on the day of week matching the given date
     *  2. Have ALL of the required skills
     *
     * Step 1 (day filtering) is done in the repository via a derived query.
     * Step 2 (skill filtering) is done here in the service because SQL doesn't
     * cleanly support "has all of these values" set semantics via derived queries,
     * and the number of results is typically small enough to filter in memory.
     *
     * @param skills the required set of skills
     * @param date   the requested service date
     * @return list of qualifying employees
     */
    @Transactional(readOnly = true)
    public List<Employee> findEmployeesForService(Set<EmployeeSkill> skills, java.time.LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Employee> availableOnDay = employeeRepository.findByDaysAvailableContains(dayOfWeek);

        // Filter to only those who have ALL the required skills
        return availableOnDay.stream()
                .filter(employee -> employee.getSkills().containsAll(skills))
                .collect(Collectors.toList());
    }
}
