package com.udacity.jdnd.course3.critter.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository for Schedule entity persistence operations.
 *
 * All queries return Schedule objects and are placed here rather than
 * in the Employee or Pet repositories, because the primary entity being
 * returned is Schedule.
 *
 * Derived queries navigate the ManyToMany and ManyToOne relationships:
 * - Schedule.pets → Pet.id
 * - Schedule.employees → Employee.id
 * - Schedule.pets → Pet.owner → Customer.id
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Finds all schedules involving a specific pet.
     */
    List<Schedule> findByPetsId(long petId);

    /**
     * Finds all schedules involving a specific employee.
     */
    List<Schedule> findByEmployeesId(long employeeId);

    /**
     * Finds all schedules involving pets owned by a specific customer.
     * Navigates: Schedule → pets → owner (Customer) → id
     */
    List<Schedule> findByPetsOwnerId(long customerId);

    /**
     * Finds all schedules for a given employee on a specific date that
     * overlap with the requested timeslot [requestedStart, requestedEnd].
     *
     * Two time windows overlap when:
     *   existing.startTime < requestedEnd AND existing.endTime > requestedStart
     *
     * Schedules with null startTime/endTime are excluded (not timeslot-based).
     * Used by the service layer to determine if an employee is already booked
     * for a given timeslot before creating a new schedule.
     *
     * @param employeeId     the employee to check
     * @param date           the date to check
     * @param requestedStart the start of the requested timeslot
     * @param requestedEnd   the end of the requested timeslot
     * @return list of conflicting schedules
     */
    @Query("SELECT s FROM Schedule s JOIN s.employees e " +
           "WHERE e.id = :employeeId " +
           "AND s.date = :date " +
           "AND s.startTime IS NOT NULL " +
           "AND s.endTime IS NOT NULL " +
           "AND s.startTime < :requestedEnd " +
           "AND s.endTime > :requestedStart")
    List<Schedule> findConflictingSchedulesForEmployee(
            @Param("employeeId") long employeeId,
            @Param("date") LocalDate date,
            @Param("requestedStart") LocalTime requestedStart,
            @Param("requestedEnd") LocalTime requestedEnd);

    /**
     * Finds all schedules on a specific date and timeslot for any employee,
     * used to find which employees are free in a given timeslot.
     *
     * @param date           the date to check
     * @param requestedStart the start of the requested timeslot
     * @param requestedEnd   the end of the requested timeslot
     * @return list of schedules that occupy that timeslot
     */
    @Query("SELECT s FROM Schedule s " +
           "WHERE s.date = :date " +
           "AND s.startTime IS NOT NULL " +
           "AND s.endTime IS NOT NULL " +
           "AND s.startTime < :requestedEnd " +
           "AND s.endTime > :requestedStart")
    List<Schedule> findSchedulesInTimeslot(
            @Param("date") LocalDate date,
            @Param("requestedStart") LocalTime requestedStart,
            @Param("requestedEnd") LocalTime requestedEnd);
}
