package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.pet.PetRepository;
import com.udacity.jdnd.course3.critter.user.Employee;
import com.udacity.jdnd.course3.critter.user.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for Schedule operations.
 *
 * Handles multi-repository coordination:
 * - Resolves Employee and Pet entities from their IDs (provided via DTO)
 * - Validates timeslot conflicts before saving (service-layer validation)
 * - Builds a complete Schedule entity before persisting
 *
 * Transaction boundary: All operations are @Transactional to ensure all
 * related data (schedule + associations) is saved atomically.
 *
 * This layer works exclusively with Schedule/Employee/Pet entities — no DTOs.
 */
@Service
@Transactional
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PetRepository petRepository;

    /**
     * Creates and persists a new schedule.
     *
     * Resolves Employee and Pet entities from the provided ID lists.
     * If an ID doesn't resolve to a valid entity, an exception is thrown
     * at the service layer (validation belongs here, not in data layer).
     *
     * If startTime and endTime are provided, validates that:
     *  1. startTime is before endTime
     *  2. No assigned employee already has a conflicting schedule that day
     *
     * @param schedule    a Schedule entity with date, activities, and optional times set
     * @param employeeIds list of employee IDs to attach to this schedule
     * @param petIds      list of pet IDs to attach to this schedule
     * @return the persisted Schedule entity with generated ID
     * @throws IllegalArgumentException if validation fails
     */
    public Schedule createSchedule(Schedule schedule, List<Long> employeeIds, List<Long> petIds) {
        // Validate that employee IDs exist
        List<Employee> employees = employeeIds.stream()
                .map(id -> employeeRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No employee found with id: " + id)))
                .collect(Collectors.toList());

        // Validate that pet IDs exist
        List<Pet> pets = petIds.stream()
                .map(id -> petRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No pet found with id: " + id)))
                .collect(Collectors.toList());

        // Validate timeslot if both startTime and endTime are provided
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            // Ensure startTime is before endTime
            if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
                throw new IllegalArgumentException(
                        "startTime must be before endTime. Got: " +
                        schedule.getStartTime() + " - " + schedule.getEndTime());
            }

            // Check each employee for timeslot conflicts on that date
            for (Employee emp : employees) {
                List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesForEmployee(
                        emp.getId(),
                        schedule.getDate(),
                        schedule.getStartTime(),
                        schedule.getEndTime());

                if (!conflicts.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Employee " + emp.getName() + " (id=" + emp.getId() +
                            ") already has a conflicting schedule on " + schedule.getDate() +
                            " from " + schedule.getStartTime() + " to " + schedule.getEndTime());
                }
            }
        }

        // Validate that schedule date is not null
        if (schedule.getDate() == null) {
            throw new IllegalArgumentException("Schedule date must not be null.");
        }

        // Validate that activities are not empty
        if (schedule.getActivities() == null || schedule.getActivities().isEmpty()) {
            throw new IllegalArgumentException("Schedule must have at least one activity.");
        }

        schedule.setEmployees(employees);
        schedule.setPets(pets);

        return scheduleRepository.save(schedule);
    }

    /**
     * Returns all schedules in the system.
     */
    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    /**
     * Returns all schedules involving a specific pet.
     *
     * @param petId the pet's ID
     * @return list of schedules for that pet
     */
    @Transactional(readOnly = true)
    public List<Schedule> getScheduleForPet(long petId) {
        return scheduleRepository.findByPetsId(petId);
    }

    /**
     * Returns all schedules involving a specific employee.
     *
     * @param employeeId the employee's ID
     * @return list of schedules for that employee
     */
    @Transactional(readOnly = true)
    public List<Schedule> getScheduleForEmployee(long employeeId) {
        return scheduleRepository.findByEmployeesId(employeeId);
    }

    /**
     * Returns all schedules involving pets owned by a specific customer.
     *
     * Navigates: Schedule → pets → owner (Customer) → id
     *
     * @param customerId the customer's ID
     * @return list of schedules for that customer's pets
     */
    @Transactional(readOnly = true)
    public List<Schedule> getScheduleForCustomer(long customerId) {
        return scheduleRepository.findByPetsOwnerId(customerId);
    }

    /**
     * Finds all employees who are available (i.e., not already scheduled) for
     * a specific timeslot on a given date AND have all required skills AND are
     * available on that day of the week.
     *
     * Strategy:
     *  1. Find employees available on that day of week with the required skills
     *     (delegated to the caller / UserService — this method accepts the candidate list)
     *  2. Find all schedules that conflict with the requested timeslot
     *  3. Subtract booked employees from the candidate list
     *
     * @param candidateEmployees employees already filtered by day + skills
     * @param date               the requested date
     * @param startTime          the requested timeslot start
     * @param endTime            the requested timeslot end
     * @return list of employees who are free in that timeslot
     */
    @Transactional(readOnly = true)
    public List<Employee> findAvailableEmployeesForTimeslot(
            List<Employee> candidateEmployees,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime) {

        // Get all schedules that conflict with the requested timeslot
        List<Schedule> conflictingSchedules = scheduleRepository.findSchedulesInTimeslot(date, startTime, endTime);

        // Collect IDs of all already-booked employees in that timeslot
        Set<Long> bookedEmployeeIds = conflictingSchedules.stream()
                .flatMap(s -> s.getEmployees().stream())
                .map(Employee::getId)
                .collect(Collectors.toSet());

        // Return candidates who are NOT booked
        return candidateEmployees.stream()
                .filter(emp -> !bookedEmployeeIds.contains(emp.getId()))
                .collect(Collectors.toList());
    }
}
