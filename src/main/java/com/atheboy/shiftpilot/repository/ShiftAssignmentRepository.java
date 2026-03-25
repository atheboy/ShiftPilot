package com.atheboy.shiftpilot.repository;

import com.atheboy.shiftpilot.domain.ShiftAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    @EntityGraph(attributePaths = {"shift", "employee"})
    List<ShiftAssignment> findAllByOrderByShiftShiftDateAscShiftStartTimeAsc();

    @EntityGraph(attributePaths = {"shift", "employee"})
    List<ShiftAssignment> findByEmployeeId(Long employeeId);
}
