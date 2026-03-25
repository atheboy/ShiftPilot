package com.atheboy.shiftpilot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String teamName;

    private int maxWeeklyHours;

    @Enumerated(EnumType.STRING)
    private ShiftType preferredShiftType;

    @OneToMany(mappedBy = "employee")
    private Set<EmployeeSkill> skills = new LinkedHashSet<>();

    @OneToMany(mappedBy = "employee")
    private Set<AvailabilityWindow> availabilityWindows = new LinkedHashSet<>();

    protected Employee() {
    }

    public Employee(String fullName, String teamName, int maxWeeklyHours, ShiftType preferredShiftType) {
        this.fullName = fullName;
        this.teamName = teamName;
        this.maxWeeklyHours = maxWeeklyHours;
        this.preferredShiftType = preferredShiftType;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public ShiftType getPreferredShiftType() {
        return preferredShiftType;
    }

    public Set<EmployeeSkill> getSkills() {
        return skills;
    }

    public Set<AvailabilityWindow> getAvailabilityWindows() {
        return availabilityWindows;
    }

    public void addSkill(Skill skill, int skillLevel) {
        this.skills.add(new EmployeeSkill(this, skill, skillLevel));
    }

    public void addAvailability(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.availabilityWindows.add(new AvailabilityWindow(this, dayOfWeek, startTime, endTime));
    }
}
