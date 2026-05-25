package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.user.Employee;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a scheduled service event.
 *
 * A Schedule links one or more Employees to one or more Pets on a specific
 * date to perform one or more activities (EmployeeSkills).
 *
 * Relationships:
 * - ManyToMany with Employee: a schedule can have multiple employees, and
 *   an employee can appear in multiple schedules.
 * - ManyToMany with Pet: a schedule can involve multiple pets, and a pet
 *   can appear in multiple schedules.
 * - ElementCollection for activities: a Set of EmployeeSkill enums, stored
 *   as strings. Set ensures no duplicate activity entries per schedule.
 *
 * Timing:
 * - date is stored as LocalDate — only the calendar day matters.
 * - startTime and endTime are stored as LocalTime — only the time-of-day
 *   within the day matters. They are optional (nullable) to maintain backwards
 *   compatibility with schedules that don't specify an exact timeslot.
 *   Using LocalTime (not LocalDateTime) avoids storing redundant date info.
 */
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The employees participating in this schedule.
     * Using a List (ordered) to match the ScheduleDTO expectation of List<Long>.
     * The join table schedule_employee links schedule and employee.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "schedule_employee",
        joinColumns = @JoinColumn(name = "schedule_id"),
        inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> employees = new ArrayList<>();

    /**
     * The pets involved in this schedule.
     * Using a List to match the ScheduleDTO expectation of List<Long>.
     * The join table schedule_pet links schedule and pet.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "schedule_pet",
        joinColumns = @JoinColumn(name = "schedule_id"),
        inverseJoinColumns = @JoinColumn(name = "pet_id")
    )
    private List<Pet> pets = new ArrayList<>();

    /**
     * The date of the scheduled service. Only the date component is needed,
     * not time-of-day, so LocalDate is the appropriate type.
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Optional start time of the service window (time-of-day only).
     * LocalTime is used — no date component required.
     * Nullable so existing schedules without explicit timeslots remain valid.
     */
    @Column(nullable = true)
    private LocalTime startTime;

    /**
     * Optional end time of the service window (time-of-day only).
     * LocalTime is used — no date component required.
     * Must be after startTime when both are present (validated in service layer).
     */
    @Column(nullable = true)
    private LocalTime endTime;

    /**
     * The set of activities to be performed on this schedule.
     * Using a Set to prevent duplicate activity entries.
     * Stored as string values in the schedule_activities join table.
     */
    @ElementCollection(targetClass = EmployeeSkill.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "schedule_activities", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "activity")
    @Enumerated(EnumType.STRING)
    private Set<EmployeeSkill> activities = new HashSet<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Set<EmployeeSkill> getActivities() {
        return activities;
    }

    public void setActivities(Set<EmployeeSkill> activities) {
        this.activities = activities;
    }
}
