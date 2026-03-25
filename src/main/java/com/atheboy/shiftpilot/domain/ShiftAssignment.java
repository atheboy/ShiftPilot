package com.atheboy.shiftpilot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private Instant assignedAt;

    protected ShiftAssignment() {
    }

    public ShiftAssignment(Shift shift, Employee employee, Instant assignedAt) {
        this.shift = shift;
        this.employee = employee;
        this.assignedAt = assignedAt;
    }

    public Long getId() {
        return id;
    }

    public Shift getShift() {
        return shift;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
