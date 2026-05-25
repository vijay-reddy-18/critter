package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.user.Employee;
import com.udacity.jdnd.course3.critter.user.EmployeeDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import com.udacity.jdnd.course3.critter.user.UserService;
import com.udacity.jdnd.course3.critter.pet.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Schedules.
 *
 * DTO ↔ Entity mapping occurs here in the controller layer.
 * ScheduleService is called with entity objects only (no DTOs passed to service).
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserService userService;

    /**
     * Creates and saves a new schedule.
     *
     * The DTO carries employee IDs and pet IDs — the service resolves
     * these IDs to actual entities before persisting.
     * Optional startTime/endTime are passed through if provided.
     *
     * @param scheduleDTO the incoming schedule data
     * @return the saved schedule as a DTO with its generated ID
     */
    @PostMapping
    public ScheduleDTO createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        Schedule schedule = convertDTOtoSchedule(scheduleDTO);
        Schedule savedSchedule = scheduleService.createSchedule(
                schedule,
                scheduleDTO.getEmployeeIds(),
                scheduleDTO.getPetIds()
        );
        return convertScheduleToDTO(savedSchedule);
    }

    /**
     * Returns all schedules in the system.
     *
     * @return list of all schedules as DTOs
     */
    @GetMapping
    public List<ScheduleDTO> getAllSchedules() {
        return scheduleService.getAllSchedules().stream()
                .map(this::convertScheduleToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all schedules involving a specific pet.
     *
     * @param petId the pet's ID
     * @return list of schedules for that pet
     */
    @GetMapping("/pet/{petId}")
    public List<ScheduleDTO> getScheduleForPet(@PathVariable long petId) {
        return scheduleService.getScheduleForPet(petId).stream()
                .map(this::convertScheduleToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all schedules involving a specific employee.
     *
     * @param employeeId the employee's ID
     * @return list of schedules for that employee
     */
    @GetMapping("/employee/{employeeId}")
    public List<ScheduleDTO> getScheduleForEmployee(@PathVariable long employeeId) {
        return scheduleService.getScheduleForEmployee(employeeId).stream()
                .map(this::convertScheduleToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all schedules involving pets owned by a specific customer.
     *
     * @param customerId the customer's ID
     * @return list of schedules for that customer's pets
     */
    @GetMapping("/customer/{customerId}")
    public List<ScheduleDTO> getScheduleForCustomer(@PathVariable long customerId) {
        return scheduleService.getScheduleForCustomer(customerId).stream()
                .map(this::convertScheduleToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Stand-out feature: Finds employees who are available (not booked) for
     * a specific timeslot on a given date AND have all the required skills.
     *
     * Request body example:
     * {
     *   "date": "2024-01-15",
     *   "startTime": "09:00",
     *   "endTime": "11:00",
     *   "skills": ["FEEDING", "WALKING"]
     * }
     *
     * @param request DTO containing date, startTime, endTime, and required skills
     * @return list of available, qualified employees as DTOs
     */
    @GetMapping("/availability/timeslot")
    public List<EmployeeDTO> findAvailableEmployeesForTimeslot(
            @RequestBody TimeslotRequestDTO request) {

        // Step 1: Find employees available on that day of week with required skills
        List<Employee> candidatesByDayAndSkill = userService.findEmployeesForService(
                request.getSkills(), request.getDate());

        // Step 2: Subtract those with conflicting schedules in the timeslot
        List<Employee> freeEmployees = scheduleService.findAvailableEmployeesForTimeslot(
                candidatesByDayAndSkill,
                request.getDate(),
                request.getStartTime(),
                request.getEndTime());

        // Convert to DTOs
        return freeEmployees.stream()
                .map(this::convertEmployeeToDTO)
                .collect(Collectors.toList());
    }

    // ==================== DTO Mapping Helpers ====================

    /**
     * Converts a ScheduleDTO (incoming) to a Schedule entity.
     * Employee and Pet associations are resolved by the service from the IDs.
     * Maps optional startTime and endTime if present.
     */
    private Schedule convertDTOtoSchedule(ScheduleDTO scheduleDTO) {
        Schedule schedule = new Schedule();
        schedule.setDate(scheduleDTO.getDate());
        schedule.setActivities(scheduleDTO.getActivities());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        return schedule;
    }

    /**
     * Converts a Schedule entity to a ScheduleDTO (outgoing response).
     * Extracts employee and pet IDs from their respective entity collections.
     * Includes startTime and endTime if they exist.
     */
    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setId(schedule.getId());
        scheduleDTO.setDate(schedule.getDate());
        scheduleDTO.setActivities(schedule.getActivities());
        scheduleDTO.setStartTime(schedule.getStartTime());
        scheduleDTO.setEndTime(schedule.getEndTime());

        // Extract employee IDs from the employee entities
        if (schedule.getEmployees() != null) {
            List<Long> employeeIds = schedule.getEmployees().stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList());
            scheduleDTO.setEmployeeIds(employeeIds);
        } else {
            scheduleDTO.setEmployeeIds(new ArrayList<>());
        }

        // Extract pet IDs from the pet entities
        if (schedule.getPets() != null) {
            List<Long> petIds = schedule.getPets().stream()
                    .map(Pet::getId)
                    .collect(Collectors.toList());
            scheduleDTO.setPetIds(petIds);
        } else {
            scheduleDTO.setPetIds(new ArrayList<>());
        }

        return scheduleDTO;
    }

    /**
     * Converts an Employee entity to an EmployeeDTO for response.
     */
    private EmployeeDTO convertEmployeeToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setSkills(employee.getSkills());
        if (employee.getDaysAvailable() != null && !employee.getDaysAvailable().isEmpty()) {
            dto.setDaysAvailable(employee.getDaysAvailable());
        }
        return dto;
    }
}
