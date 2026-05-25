package com.udacity.jdnd.course3.critter.user;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an employee who can perform services for pets.
 *
 * Uses a separate table from Customer (composition, not inheritance).
 * Employees have:
 *  - A set of skills (EmployeeSkill enums) stored as strings in a join table
 *  - A set of available days (DayOfWeek enums) stored as strings in a join table
 *
 * ElementCollection is used for skills and daysAvailable because they are
 * simple value sets (not entities), and Set enforces uniqueness.
 *
 * Enum values are stored as STRING (not ordinal) to be resilient to enum
 * reordering or additions in the future.
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 500)
    private String name;

    /**
     * Set of skills (e.g. FEEDING, WALKING). Using Set enforces uniqueness —
     * an employee shouldn't have duplicate skill entries.
     * Stored in a separate join table: employee_skills.
     */
    @ElementCollection(targetClass = EmployeeSkill.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "skill")
    @Enumerated(EnumType.STRING)
    private Set<EmployeeSkill> skills = new HashSet<>();

    /**
     * Days the employee is available to work.
     * Using Set for uniqueness — no sense having MON listed twice.
     * Stored in a separate join table: employee_days_available.
     */
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_days_available", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysAvailable = new HashSet<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<EmployeeSkill> getSkills() {
        return skills;
    }

    public void setSkills(Set<EmployeeSkill> skills) {
        this.skills = skills;
    }

    public Set<DayOfWeek> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(Set<DayOfWeek> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }
}
