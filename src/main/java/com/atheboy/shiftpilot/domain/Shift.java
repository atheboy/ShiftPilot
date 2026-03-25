package com.atheboy.shiftpilot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate shiftDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private ShiftType shiftType;

    @Enumerated(EnumType.STRING)
    private ShiftPriority priority;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_skill_id")
    private Skill requiredSkill;

    private int requiredHeadcount;

    private String notes;

    @OneToMany(mappedBy = "shift")
    private Set<ShiftAssignment> assignments = new LinkedHashSet<>();

    protected Shift() {
    }

    public Shift(
            LocalDate shiftDate,
            LocalTime startTime,
            LocalTime endTime,
            ShiftType shiftType,
            ShiftPriority priority,
            String location,
            Skill requiredSkill,
            int requiredHeadcount,
            String notes
    ) {
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftType = shiftType;
        this.priority = priority;
        this.location = location;
        this.requiredSkill = requiredSkill;
        this.requiredHeadcount = requiredHeadcount;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public ShiftPriority getPriority() {
        return priority;
    }

    public String getLocation() {
        return location;
    }

    public Skill getRequiredSkill() {
        return requiredSkill;
    }

    public int getRequiredHeadcount() {
        return requiredHeadcount;
    }

    public String getNotes() {
        return notes;
    }

    public Set<ShiftAssignment> getAssignments() {
        return assignments;
    }

    public void addAssignment(Employee employee, Instant assignedAt) {
        this.assignments.add(new ShiftAssignment(this, employee, assignedAt));
    }
}
