package com.udacity.jdnd.course3.critter.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Repository for Employee entity persistence operations.
 *
 * Provides standard CRUD via JpaRepository plus a derived query to find
 * employees available on a specific day of week.
 *
 * The skill filtering (finding employees with ALL required skills) is done
 * in the Service layer after fetching candidates by day, because SQL does
 * not natively support "contains all" set queries cleanly via derived methods.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Finds all employees who are available on the given day of week.
     * Derived query path: Employee.daysAvailable (Set<DayOfWeek>) contains the value.
     */
    List<Employee> findByDaysAvailableContains(DayOfWeek dayOfWeek);
}
