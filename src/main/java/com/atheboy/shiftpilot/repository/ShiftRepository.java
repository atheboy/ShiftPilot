package com.atheboy.shiftpilot.repository;

import com.atheboy.shiftpilot.domain.Shift;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @EntityGraph(attributePaths = {"requiredSkill", "assignments.employee"})
    List<Shift> findAllByOrderByShiftDateAscStartTimeAsc();

    @EntityGraph(attributePaths = {"requiredSkill", "assignments.employee"})
    Optional<Shift> findById(Long id);
}
