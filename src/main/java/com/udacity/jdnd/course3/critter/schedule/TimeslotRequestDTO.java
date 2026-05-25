package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.user.EmployeeSkill;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Request DTO for the timeslot availability endpoint.
 *
 * Used by GET /schedule/availability/timeslot to find employees who:
 *  1. Are available on the day of week for the given date
 *  2. Have all required skills
 *  3. Are NOT already booked during the startTime–endTime window
 */
public class TimeslotRequestDTO {

    /** The date of the requested service. */
    private LocalDate date;

    /** The start of the requested service timeslot (time-of-day). */
    private LocalTime startTime;

    /** The end of the requested service timeslot (time-of-day). */
    private LocalTime endTime;

    /** The set of skills required for the service. */
    private Set<EmployeeSkill> skills;

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

    public Set<EmployeeSkill> getSkills() {
        return skills;
    }

    public void setSkills(Set<EmployeeSkill> skills) {
        this.skills = skills;
    }
}
